package piu.utils

import kotlin.collections.mutableListOf
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.Encoding
import com.knuddels.jtokkit.api.EncodingRegistry
import com.knuddels.jtokkit.api.EncodingType
import com.knuddels.jtokkit.api.IntArrayList
import java.util.PriorityQueue
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.pow
import kotlin.math.sqrt

data class VectorRecord(
    val id: String,
    val text: String,
    val embedding: FloatArray,
) {
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
        val chunks: MutableList<String> = mutableListOf()
        var start = 0

        while (start < tokens.size()) {
            val end = minOf(start + chunkSize, tokens.size())
            val chunkTokens = IntArrayList(end - start)


            for (i in start until end) {
                chunkTokens.add(tokens.get(i))
            }

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

        require(embedding.size == dimensions) { "Vector dimension mismatch!" }
        records.add(VectorRecord(id.toString(), text, embedding))
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
        var sumOfSquares = 0.0f
        for (i in v1.indices) {
            val diff = v1[i] - v2[i]
            sumOfSquares += diff.pow(2)
        }
        return sqrt(sumOfSquares)
    }

    private fun dotProduct(v1: FloatArray, v2: FloatArray): Float {
        require(v1.size == v2.size) { "Vector dimension mismatch!" }
        var dotProd = 0.0f
        for (i in v1.indices) {
            dotProd += v1[i] * v2[i]
        }
        return dotProd
    }

    fun search(queryVector: FloatArray, topK: Int): List<SearchResult> {

        require(queryVector.size == dimensions) { "Query vector dimension mismatch!" }

        return records.asSequence()
            .map { record ->
                val score = computeCosineSimilarity(queryVector, record.embedding)
                SearchResult(record, score)
            }
            .sortedByDescending { it.score }
            .take(topK)
            .toList()
    }

    data class TextNode(
        var chunkId: String,
        var embedding: FloatArray,
        var textContent: String,
        var g: Double = 0.0,
        var h: Double = 0.0,
        val parent: TextNode? = null
    ) : Comparable<TextNode> {
        val f: Double get() = g + h
        override fun compareTo(other: TextNode): Int = this.f.compareTo(other.f)
    }

    // Missing heuristic method logic wrapper
    private fun computeHybridHeuristic(v1: FloatArray, v2: FloatArray, alpha: Double, beta: Double): Double {
        val euclid = computeEuclideanDistance(v1, v2).toDouble()
        val cosineDist = 1.0 - computeCosineSimilarity(v1, v2).toDouble()
        return (alpha * euclid) + (beta * cosineDist)
    }

    fun aStarEuclidCos(
        startEmbedding: FloatArray,
        goalEmbedding: FloatArray,
        vectorDbQuery: (FloatArray) -> List<TextNode>, //some function that takes float array
        alpha: Double = 0.5,
        beta: Double = 0.5
    ): List<TextNode>? {

        val openSet = PriorityQueue<TextNode>(Comparator.comparingDouble { it.f })
        val closedSet = HashSet<String>()
        val bestGInstance = HashMap<String, Double>()

        val startNode = TextNode(
            chunkId = "START",
            embedding = startEmbedding,
            textContent = "Initial Query",
            g = 0.0,
            h = computeHybridHeuristic(startEmbedding, goalEmbedding, alpha, beta)
        )

        openSet.add(startNode)
        bestGInstance[startNode.chunkId] = 0.0

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()

            if (computeEuclideanDistance(current.embedding, goalEmbedding) < 0.05f) {
                return reconstructPath(current)
            }

            if (closedSet.contains(current.chunkId)) continue
            closedSet.add(current.chunkId)

            val relatedChunks = vectorDbQuery(current.embedding)

            for (neighbor in relatedChunks) {
                if (closedSet.contains(neighbor.chunkId)) continue

                val stepCost = computeEuclideanDistance(current.embedding, neighbor.embedding).toDouble()
                val tentativeG = current.g + stepCost

                if (tentativeG >= (bestGInstance[neighbor.chunkId] ?: Double.MAX_VALUE)) {
                    continue
                }

                val hScore = computeHybridHeuristic(neighbor.embedding, goalEmbedding, alpha, beta)

                neighbor.g = tentativeG
                neighbor.h = hScore

                val updatedNeighbor = neighbor.copy(parent = current)

                bestGInstance[neighbor.chunkId] = tentativeG
                openSet.add(updatedNeighbor)
            }
        }

        return null
    }

    private fun reconstructPath(node: TextNode?): List<TextNode> {
        val path = mutableListOf<TextNode>()
        var current = node
        while (current != null) {
            path.add(0, current)
            current = current.parent
        }
        return path
    }

    // private fun recursivePath(node: TextNode?, list: MutableListOf<TextNode>): MutableListOf<TextNode> {
    //     if (node == null) {
    //         return list
    //     }
    //     list.add(0, node)
    //     return recursivePath(node.parent, list)
    // }
}
