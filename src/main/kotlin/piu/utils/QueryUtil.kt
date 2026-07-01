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

    private fun getConnection(): Connection {
        val url = databaseUrl.orElseThrow {
            IllegalStateException("Database URL configuration 'vectordb.url' is missing!")
        }
        val conn = DriverManager.getConnection(url)
        PGvector.addVectorType(conn)
        return conn
    }

    fun initDatabaseSchema() {
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector")

                // Build a structured table for A* TextNodes
                stmt.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS legal_chunks (
                        chunk_id VARCHAR(255) PRIMARY KEY,
                        text_content TEXT NOT NULL,
                        embedding vector(768) NOT NULL
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

        getConnection().use { conn ->
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, chunkId)
                pstmt.setString(2, text)
                // Wrap the primitive FloatArray into pgvector's custom PGvector object
                pstmt.setObject(3, PGvector(embedding))
                pstmt.executeUpdate()
            }
        }
    }

    fun queryNearestNeighbors(queryVector: FloatArray, limit: Int = 5): List<VectorIndex.TextNode> {
        val sql = """
            SELECT chunk_id, text_content, embedding, (embedding <-> ?) as distance
            FROM legal_chunks
            ORDER BY distance ASC
            LIMIT ?
        """.trimIndent()

        val results = mutableListOf<VectorIndex.TextNode>()

        getConnection().use { conn ->
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setObject(1, PGvector(queryVector))
                pstmt.setInt(2, limit)

                pstmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        val chunkId = rs.getString("chunk_id")
                        val textContent = rs.getString("text_content")
                        val pgVectorObj = rs.getObject("embedding") as PGvector

                        // Fixed parser logic with clean smart-casting
                        fun parsePgVector(vectorStr: String?): FloatArray {
                            val clean = vectorStr?.trim('[', ']')
                            if (clean.isNullOrEmpty()) return floatArrayOf()

                            // Counting elements by parsing commas
                            var count = 1
                            for (i in 0 until clean.length) {
                                if (clean[i] == ',') count++
                            }

                            val result = FloatArray(count)
                            var index = 0
                            var start = 0

                            for (i in 0 until clean.length) {
                                if (clean[i] == ',') {
                                    result[index++] = clean.substring(start, i).trim().toFloat()
                                    start = i + 1
                                }
                            }
                            result[index] = clean.substring(start).trim().toFloat()

                            return result
                        }

                        val floatArrayBytes = parsePgVector(pgVectorObj.value)

                        results.add(
                            VectorIndex.TextNode(
                                chunkId = chunkId,
                                embedding = floatArrayBytes,
                                textContent = textContent,
                                g = 0.0,
                                h = 0.0
                            )
                        )
                    }
                }
            }
        }
        return results
    }
}
