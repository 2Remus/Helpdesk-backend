package piu.controllers

import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import piu.models.GenerationConfig
import piu.models.ModelType
import piu.utils.*
import kotlin.math.sin
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking


@Path("/api/search-test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class TestResource {

    @Inject
    lateinit var vectorRepository: VectorRepository

    // --- Data Transfer Objects ---

    data class SearchTestRequest(
        val sampleDocument: String,
        val alpha: Double = 0.5,
        val beta: Double = 0.5
    )

    data class PromptRequest(
        val prompt: String,
        val alpha: Double = 0.5,
        val beta: Double = 0.5,
        val limit: Int = 3
    )


    private fun generateDocumentHash(text: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(text.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }.take(12) // Use a clean 12-char prefix
    }

    // --- Endpoint 1: Seed & Test ---
    @POST
    @Path("/run-with-seeding")
    fun runPipelineTest(request: SearchTestRequest): Response {
        val embedder = LocalEmbeddingProvider()
        try {
            //vectorRepository.initDatabaseSchema()
            // Chunker handling
            val chunker = SemanticChunker(chunkSize = 100, chunkOverlap = 30)
            var chunks = chunker.splitText(request.sampleDocument)

            if (chunks.size <= 1 && request.sampleDocument.length > 1200) {
                chunks = request.sampleDocument.chunked(1200)
            }

            if (chunks.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("message" to "Sample document yielded no chunks."))
                    .build()
            }

            // --- AUTOMATIC UNIQUE DOCUMENT SIGNATURE ---
            val docSignature = generateDocumentHash(request.sampleDocument)
            val insertedIds = mutableListOf<String>()

            // 1. Batch execute ALL embeddings from Ollama at once to avoid loop latency
            val allEmbeddings: List<FloatArray> = kotlinx.coroutines.runBlocking {
                embedder.getEmbedding(chunks)
            }

            val connection = vectorRepository.createConnection()
            try {
                // 2. Safely cycle through the pre-computed embedding lists and save them
                chunks.forEachIndexed { index, text ->
                    val chunkId = "doc_${docSignature}_chunk_$index"
                    insertedIds.add(chunkId)

                    val emb = allEmbeddings[index]
                    vectorRepository.insertVectorRecord(chunkId, text, emb)
                }
            } finally {
                connection.close()
            }

            return Response.ok(
                mapOf(
                    "status" to "Success",
                    "message" to "Database successfully seeded via batch operation.",
                    "chunksSeededCount" to chunks.size,
                    "generatedIds" to insertedIds
                )
            ).build()

        } catch (e: Exception) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message, "stack" to e.stackTraceToString()))
                .build()
        }
    }


    @POST
    @Path("/process-prompt")
    suspend fun processPrompt(request: PromptRequest): Response {
        try {
            val vectorIndex = VectorIndex(dimensions = 1024)
            val embedder = LocalEmbeddingProvider()
            val promptText = request.prompt
            val newText = kotlinx.coroutines.runBlocking {
                vectorIndex.decomposeQuery(promptText)
            }

            // FIX 1: Change to getEmbeddings to match the List<String> input type from decomposition
            val queryEmbeddings: List<FloatArray> = kotlinx.coroutines.runBlocking {
                embedder.getEmbedding(newText)
            }

            vectorRepository.createConnection().use { sharedConnection ->
                // FIX 2: Iterating over queryEmbeddings cleanly maps each search vector
                val baseScan = queryEmbeddings.flatMap { individualEmbedding ->
                    vectorRepository.queryNearestNeighbors(
                        individualEmbedding,
                        limit = request.limit,
                        externalConn = sharedConnection
                    )
                }

                if (baseScan.isEmpty()) {
                    return Response.status(Response.Status.NOT_FOUND)
                        .entity(mapOf("message" to "No context found in vector database."))
                        .build()
                }

                // 2. Unify, cross-reference, and deduplicate nodes matching all sub-topics
                val finalNodes = baseScan.distinctBy { it.chunkId }

                // 3. Stitch multiple dense matching chunks together for the LLM context window
                val aggregatedContext = finalNodes
                    .take(5)
                    .joinToString("\n\n") { "--- Context (${it.chunkId}) ---\n${it.textContent.trim()}" }

                // 4. Send the combined multi-document context straight to your Ollama handler
                val dataHandler = DataHandler()
                val targetModelType = ModelType.CUSTOMS

                val payload = dataHandler.parseData(
                    prompt = """
                        You are a precise assistant answering questions based strictly on the provided context.

                        [Context Start]
                        $aggregatedContext
                        [Context End]

                        Instructions:
                        1. Answer the question using ONLY the factual information provided in the context above.
                        2. If the context does not contain the answer, reply with: "I cannot find the answer in the provided documents."
                        3. Provide a clear, detailed multi-sentence response.

                        Question: ${request.prompt}
                        Answer:
                        """.trimIndent(),
                    config = GenerationConfig(),
                    model = "llama3.2:1b",
                    modelip = targetModelType
                )

                val ollamaHandler = OllamaHandler()
                val responseText = runBlocking {
                    val ioResponse = ollamaHandler.generateResponse(payload)
                    ioResponse?.bodyAsText() ?: "Failed to get response from Ollama"
                }

                return Response.ok(
                    mapOf(
                        "status" to "Success",
                        "retrievedContext" to finalNodes.map { mapOf("id" to it.chunkId, "text" to it.textContent) },
                        "llmReply" to responseText
                    )
                ).build()
            }

        } catch (e: Exception) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message, "stack" to e.stackTraceToString()))
                .build()
        }
    }
}
