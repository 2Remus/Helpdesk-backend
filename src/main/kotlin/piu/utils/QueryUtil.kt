package piu.utils

import java.sql.Connection
import java.sql.DriverManager
import java.util.Optional
import org.eclipse.microprofile.config.inject.ConfigProperty
import com.pgvector.PGvector
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class VectorRepository {

    @Inject
    @ConfigProperty(name = "vectordb.url")
    lateinit var databaseUrl: Optional<String>

    // Core helper method to create a fresh connection when needed
    fun createConnection(): Connection {
        val url = databaseUrl.orElseThrow {
            IllegalStateException("Database URL configuration 'vectordb.url' is missing!")
        }
        val conn = DriverManager.getConnection(url)
        PGvector.addVectorType(conn)
        return conn
    }

    // Accepts an optional connection parameter to prevent pooling exhaustions
    fun findNodeById(id: String, externalConn: Connection? = null): VectorIndex.TextNode? {
        val sql = "SELECT chunk_id, text_content, embedding FROM legal_chunks WHERE chunk_id = ?"

        // If an external connection is provided, do NOT close it when done!
        val stmt = (externalConn ?: createConnection()).prepareStatement(sql)

        return stmt.use { pstmt ->
            pstmt.setString(1, id)
            pstmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val vectorString = rs.getString("embedding").trim('[', ']')
                    val floatArray = vectorString.split(",").map { it.trim().toFloat() }.toFloatArray()

                    VectorIndex.TextNode(
                        chunkId = rs.getString("chunk_id"),
                        textContent = rs.getString("text_content"),
                        embedding = floatArray
                    )
                } else null
            }
        }.also {
            // Only close the connection if we generated it locally inside this method
            if (externalConn == null) stmt.connection.close()
        }
    }

    // Accepts an optional connection parameter to stay stable within tight loops
    fun queryNearestNeighbors(
        queryVector: FloatArray,
        limit: Int = 5,
        externalConn: Connection? = null
    ): List<VectorIndex.TextNode> {
        val sql = """
            SELECT chunk_id, text_content, embedding, (embedding <=> ?) as distance
            FROM legal_chunks
            ORDER BY distance ASC
            LIMIT ?
        """.trimIndent()

        val results = mutableListOf<VectorIndex.TextNode>()
        val stmt = (externalConn ?: createConnection()).prepareStatement(sql)

        try {
            stmt.use { pstmt ->
                pstmt.setObject(1, PGvector(queryVector))
                pstmt.setInt(2, limit)

                pstmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        val chunkId = rs.getString("chunk_id")
                        val textContent = rs.getString("text_content")
                        val pgVectorObj = rs.getObject("embedding") as PGvector

                        val floatArrayBytes = try {
                            pgVectorObj.toArray()
                        } catch (e: NoSuchMethodError) {
                            fastParsePgVector(pgVectorObj.value)
                        }

                        results.add(
                            VectorIndex.TextNode(
                                chunkId = chunkId,
                                embedding = floatArrayBytes,
                                textContent = textContent
                            )
                        )
                    }
                }
            }
        } finally {
            // Only close the connection if we generated it locally inside this method
            if (externalConn == null) stmt.connection.close()
        }
        return results
    }

    private fun fastParsePgVector(vectorStr: String?): FloatArray {
        val clean = vectorStr?.trim('[', ']') ?: return floatArrayOf()
        val tokens = clean.split(",")
        val result = FloatArray(tokens.size)
        for (i in tokens.indices) {
            result[i] = tokens[i].trim().toFloat()
        }
        return result
    }

    fun initDatabaseSchema() {
        createConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector")
                stmt.executeUpdate(
                    """

                    DROP TABLE legal_chunks;
                    CREATE TABLE IF NOT EXISTS legal_chunks (
                        chunk_id VARCHAR(255) PRIMARY KEY,
                        text_content TEXT NOT NULL,
                        embedding vector(1024) NOT NULL
                    )
                """.trimIndent()
                )
            }
        }
    }

    fun insertVectorRecord(chunkId: String, text: String, embedding: FloatArray) {
        val sql = """



            INSERT INTO legal_chunks (chunk_id, text_content, embedding)
            VALUES (?, ?, ?)
            ON CONFLICT (chunk_id)
            DO UPDATE SET text_content = EXCLUDED.text_content, embedding = EXCLUDED.embedding
        """.trimIndent()

        createConnection().use { conn ->
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, chunkId)
                pstmt.setString(2, text)
                pstmt.setObject(3, PGvector(embedding))
                pstmt.executeUpdate()
            }
        }
    }
}
