package piu.utils

import io.quarkus.runtime.Quarkus
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
import io.quarkus.logging.Log
import piu.models.ModelType
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import piu.models.GenerationConfig

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

    // fun aStarEuclidCos(
    //     startEmbedding: FloatArray,
    //     goalEmbedding: FloatArray,
    //     vectorDbQuery: (FloatArray) -> List<TextNode>, //some function that takes float array
    //     alpha: Double = 0.5,
    //     beta: Double = 0.5
    // ): List<TextNode>? {

    //     val openSet = PriorityQueue<TextNode>(Comparator.comparingDouble { it.f })
    //     val closedSet = HashSet<String>()
    //     val bestGInstance = HashMap<String, Double>()

    //     val startNode = TextNode(
    //         chunkId = "START",
    //         embedding = startEmbedding,
    //         textContent = "Initial Query",
    //         g = 0.0,
    //         h = computeHybridHeuristic(startEmbedding, goalEmbedding, alpha, beta)
    //     )

    //     openSet.add(startNode)
    //     bestGInstance[startNode.chunkId] = 0.0

    //     while (openSet.isNotEmpty()) {
    //         val current = openSet.poll()

    //         if (computeEuclideanDistance(current.embedding, goalEmbedding) < 0.25f) {
    //             return reconstructPath(current)
    //         }

    //         if (closedSet.contains(current.chunkId)) continue
    //         closedSet.add(current.chunkId)

    //         val relatedChunks = vectorDbQuery(current.embedding)


    //         for (neighbor in relatedChunks) {
    //             if (closedSet.contains(neighbor.chunkId)) continue

    //             val neighborToGoalSimilarity = computeCosineSimilarity(neighbor.embedding, goalEmbedding)

    //             if (neighborToGoalSimilarity < 0.4f) {
    //                 continue
    //             }

    //             //val stepCost = computeEuclideanDistance(current.embedding, neighbor.embedding).toDouble()
    //             val stepCost = computeHybridHeuristic(current.embedding, neighbor.embedding, alpha, beta)
    //             val tentativeG = current.g + stepCost

    //             if (tentativeG >= (bestGInstance[neighbor.chunkId] ?: Double.MAX_VALUE)) {
    //                 continue
    //             }

    //             val hScore = computeHybridHeuristic(neighbor.embedding, goalEmbedding, alpha, beta)

    //             neighbor.g = tentativeG
    //             neighbor.h = hScore

    //             val updatedNeighbor = neighbor.copy(parent = current)

    //             bestGInstance[neighbor.chunkId] = tentativeG
    //             openSet.add(updatedNeighbor)
    //         }
    //     }

    //     return null
    // }
    //
    //
    //
    //

    suspend fun decomposeQuery(userPrompt: String): List<String> {
        val systemPrompt = """
            You are a search query planner. Break down the user's input into 1 to 3 simple, distinct keyword search targets.
            Output ONLY a raw JSON array of strings. Do not include markdown blocks, text formatting, or explanations.

            Example Input: Compare Macbeth and Napoleon the pig
            Example Output: ["Macbeth", "Napoleon the pig"]
        """.trimIndent()

        try {
            val dataHandler = DataHandler()
            val ollamaHandler = OllamaHandler()
            val targetModelType = ModelType.CUSTOMS // Match your Main IP config slot

            // 1. Package the strict parsing payload using your DataHandler template match
            val payload = dataHandler.parseData(
                prompt = "$systemPrompt\n\nInput: $userPrompt\nOutput:",
                config = GenerationConfig(
                    temp = 0.0f // Lock temperature to 0.0 for rigid deterministic JSON shapes
                ),
                model = "llama3.2:1b",
                modelip = targetModelType
            )

            // 2. Fire the raw request out through your Ktor wrapper
            val ioResponse = ollamaHandler.generateResponse(payload)
            val rawJson = ioResponse?.bodyAsText() ?: ""

            // 3. Strip away unwanted Markdown fences that 1b models sneak in
            val cleanedResponse = rawJson
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            return Json.decodeFromString<List<String>>(cleanedResponse)
        } catch (e: Exception) {
            println("⚠️ Query Decomposition failed parsing JSON. Falling back to raw prompt. Error: ${e.message}")
            return listOf(userPrompt)
        }
    }

    fun aStarEuclidCos(
        startEmbedding: FloatArray,
        goalEmbedding: FloatArray,
        vectorDbQuery: (FloatArray) -> List<TextNode>,
        fetchNodeById: ((String) -> TextNode?)? = null,
        alpha: Double = 0.5,
        beta: Double = 0.5
    ): List<TextNode>? {

        val openSet = PriorityQueue<TextNode>(128, Comparator.comparingDouble { it.f })
        val closedSet = HashSet<String>(256)
        val bestGInstance = HashMap<String, Double>(256)

        var bestTargetNode: TextNode? = null

        val initialSeeds = vectorDbQuery(startEmbedding)
        for (seed in initialSeeds) {
            val hScore = computeHybridHeuristic(seed.embedding, goalEmbedding, alpha, beta)
            val seedNode = seed.copy(g = 0.0, h = hScore)
            openSet.add(seedNode)
            bestGInstance[seed.chunkId] = 0.0

            if (bestTargetNode == null || hScore < bestTargetNode.h) {
                bestTargetNode = seedNode
            }
        }

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()
            if (current.g > (bestGInstance[current.chunkId] ?: Double.MAX_VALUE)) continue

            if (bestTargetNode == null || current.h < bestTargetNode.h) {
                bestTargetNode = current
            }
            if (!closedSet.add(current.chunkId)) continue

            val relatedChunks = ArrayList<TextNode>(2)
            if (fetchNodeById != null && current.chunkId.startsWith("chunk_")) {
                val currentIdNum = current.chunkId.substring(6).toIntOrNull()

                if (currentIdNum != null) {
                    val nextId = "chunk_${currentIdNum + 1}"
                    val prevId = "chunk_${currentIdNum - 1}"

                    if (!closedSet.contains(nextId)) {
                        fetchNodeById(nextId)?.let { relatedChunks.add(it) }
                    }

                    if (!closedSet.contains(prevId)) {
                        fetchNodeById(prevId)?.let { relatedChunks.add(it) }
                    }
                }
            }

            for (neighbor in relatedChunks) {
                if (closedSet.contains(neighbor.chunkId)) continue
                val similarity = dotProduct(current.embedding, neighbor.embedding)
                val baseStepCost = 1.0 - similarity
                val depthFactor = 1.0 + (current.g * 0.40)
                val stepCost = (baseStepCost + 0.15) * depthFactor
                val tentativeG = current.g + stepCost

                val currentBestG = bestGInstance[neighbor.chunkId] ?: Double.MAX_VALUE
                if (tentativeG >= currentBestG) continue

                val hScore = computeHybridHeuristic(neighbor.embedding, goalEmbedding, alpha, beta)

                val updatedNeighbor = neighbor.copy(
                    parent = current,
                    g = tentativeG,
                    h = hScore
                )

                bestGInstance[neighbor.chunkId] = tentativeG
                openSet.add(updatedNeighbor)
            }
        }
        return bestTargetNode?.let { reconstructPath(it) }
    }

    fun reconstructPath(node: TextNode): List<TextNode> {
        val path = mutableListOf<TextNode>()
        var current: TextNode? = node
        while (current != null) {
            path.add(0, current) // Add to the front to maintain chronological order
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
