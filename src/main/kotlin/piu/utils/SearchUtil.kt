package piu.utils


import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.Encoding
import com.knuddels.jtokkit.api.EncodingRegistry
import com.knuddels.jtokkit.api.EncodingType
import com.knuddels.jtokkit.api.IntArrayList
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.pow
import kotlin.math.sqrt

data class VectorRecord(
    val id: String,
    val text: String,
    val embedding: FloatArray,
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VectorRecord
        return id == other.id
    }
    override fun hashCode(): Int = id.hashCode()
}

data class SearchResult(
    val record: VectorRecord,
    val score: Float,
)

class SemanticChunker(
    private val chunkSize: Int,
    private val chunkOverlap: Int
) {
    private val registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry()
    private val encoder: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)

    init {
        require(chunkOverlap < chunkSize) { "Overlap must be smaller than chunk size!" }
    }

    fun splitText(text: String): List<String> {
        val tokens: IntArrayList = encoder.encode(text)
        val chunks: MutableList<String> = mutableListOf<String>()
        var start: Int = 0

        while (start < tokens.size()) {
            val end: Int = minOf(start + chunkSize, tokens.size())
            val chunkTokens = IntArrayList(end - start)

            // Decode back into a raw string slice
            chunks.add(encoder.decode(chunkTokens))

            if (end == tokens.size()) break
            start += chunkSize - chunkOverlap
        }
        return chunks
    }
}


class VectorIndex(private val dimensions: Int) {
    private val records = CopyOnWriteArrayList<VectorRecord>()

    fun insert(id: Long, text: String, embedding: FloatArray) {
        debug_assert(embedding.size == dimensions) { "Vector dimension mismatch!" }

    }

    private fun computeCosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f

        for (i in v1.indices) {
            val a = v1[i]
            val b = v2[i]
            dotProduct += a * b
            normA += a * a
            normB += b * b
        }

        if (normA == 0.0f || normB == 0.0f) return 0.0f
        return dotProduct / (sqrt(normA) * sqrt(normB))
    }

    private fun computeEuclideanDistance(v1: FloatArray, v2: FloatArray): Float {
        var sum_of_squares = 0.0f
        for (i in v1.indices) {
            val diff = v1[i] - v2[i]
            val diff_sq = diff.pow(2)
            sum_of_squares += diff_sq

        }
        val distance: Float = sqrt(sum_of_squares)
        return distance
    }

    private fun dotProduct(v1: FloatArray, v2: FloatArray): Float {
        require(v1.size == v2.size) { "Vector dimension mismatch!" }
        var dotProduct: Float = 0.0f
        for (i in v1.indices) {
            val product = v1[i] * v2[i]
            dotProduct += product
        }



        return dotProduct
    }


    //fix this pls
    fun search(queryVector: FloatArray, topK: Int): List<SearchResult> {
        require(queryVector.size != dimensions) { "Query vector dimension mismatch!" }

        return records.asSequence()
            .map { record ->
                val score = computeCosineSimilarity(queryVector, record.embedding)
                SearchResult(record, score)
            }
            // Sort descending by similarity score
            .sortedByDescending { it.score }
            .take(topK)
            .toList()
    }

    data class TextNode(
        var chunkid: String,
        var embedding: DoubleArray,
        var textContent: String,
        var g: Double = 0.0,
        var h: Double = 0.0,
        val parent TextNode? = null
    ): Comparable<TextNode> {
        val f: Double get() = g + h
        override fun compareTo(other: TextNode): Int = this.f.compareTo(other.f)
    }

    // A* implementation
    fun aStarSearch(start: DoubleArray, goal: DoubleArray, vectorDbQuery: (DoubleArray) -> List<TextNode>): List<TextNode>{
        val openSet = PriorityQueue<TextNode>()
        val closedSet = mutableListOf<String>()



        val startnode = TextNode(
            chunkid = "START"
            embedding = start
            textContent = "Initial Query"
            g = 0.0,
            h = computeEuclideanDistance(start, goal)
        )

        openSet.add(startnode)

        while (openSet.isNotEmpty){
            val current = openSet.poll()
            if (computeEuclideanDistance(current.embedding, goalEmbedding) < 0.25) {
                       return reconstructPath(current)
            }

            if (closedSet.contains(current.chunkId)) continue
            closedSet.add(current.chunkId)

            val relateChunks = vectorDbQuery(current.embedding)

            for (neighbor in relatedChunks) {
                if (closedSet.contains(neighbor.chunkId) continue

                val setpCost = computeEuclideanDistance(current.embedding, neighbor.embedding)
                val tentativeG = current.g + setpCost

                val h = computeEuclideanDistance(neighbor.embedding, goalEmbedding)

                neighbor.g = tentativeG
                neighbor.h = h
                val updatedNeighbor = neighbor.copy(parent = current)

                openSet.add(updatedNeighbor)
            }

        }

        return null
    }

    //itterative method
    private fun reconstructPath(node: TextNode?, list): List<TextNode>{
        val path = mutableListOf<TextNode>()
        var current = node
        while (current != null){
            path.add(0, current)
            current = current.parent
        }

        return path
    }


    //recursiveMethod
    private fun recurisivePath(node: TextNode?, list: MutableListOf<TextNode>): MutableListOf<TextNode>{

        if (node == null){
            return list
        }
        list.add(0, node)
        return recurisivePath(node.parent, list)
    }
}
