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

@Path("/api/search-test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class TestResource {

    @Inject
    lateinit var vectorRepository: VectorRepository

    data class SearchTestRequest(
        val sampleDocument: String, // Text to seed into the empty DB
        val alpha: Double = 0.5,
        val beta: Double = 0.5
    )

    @POST
    @Path("/run-with-seeding")
    fun runPipelineTest(request: SearchTestRequest): Response {
        try {
            // 1. Ensure schema exists
            vectorRepository.initDatabaseSchema()

            // 2. Use your SemanticChunker to split the provided sample text
            // Chunk size of 100 tokens, overlap of 20 tokens
            val chunker = SemanticChunker(chunkSize = 100, chunkOverlap = 20)
            val chunks = chunker.splitText(request.sampleDocument)

            if (chunks.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("message" to "Sample document yielded no chunks."))
                    .build()
            }

            // 3. Seed chunks into the database with simulated embeddings
            // We'll generate slightly different embeddings for each chunk so A* can traverse them
            val chunkIds = mutableListOf<String>()
            chunks.forEachIndexed { index, text ->
                val chunkId = "chunk_$index"
                chunkIds.add(chunkId)

                // Mocking a 768-dimension embedding that shifts progressively
                val mockEmbedding = FloatArray(768) { i ->
                    (sin((index + i).toDouble()) * 0.5 + 0.5).toFloat()
                }

                vectorRepository.insertVectorRecord(chunkId, text, mockEmbedding)
            }

            // 4. Define Start and Goal states for A* using your 768 dimension rule
            val vectorIndex = VectorIndex(dimensions = 768)

            // Start close to the first chunk's pattern, Goal close to the last chunk's pattern
            val startEmbedding = FloatArray(768) { i -> (sin((0 + i).toDouble()) * 0.5 + 0.5).toFloat() }
            val goalEmbedding = FloatArray(768) { i -> (sin(((chunks.size - 1) + i).toDouble()) * 0.5 + 0.5).toFloat() }

            // 5. Execute A* search drawing directly from the newly seeded database
            val pathNodes = vectorIndex.aStarEuclidCos(
                startEmbedding = startEmbedding,
                goalEmbedding = goalEmbedding,
                vectorDbQuery = { currentVector ->
                    // Pulls nearest neighbor chunks from DB based on current vector state
                    vectorRepository.queryNearestNeighbors(currentVector, limit = 3)
                },
                alpha = request.alpha,
                beta = request.beta
            )

            if (pathNodes.isNullOrEmpty()) {
                return Response.ok(
                    mapOf(
                        "message" to "Data seeded successfully, but no complete A* path could connect start to goal.",
                        "seededChunksCount" to chunks.size
                    )
                ).build()
            }

            // 6. Aggregate path context and format for LLM pipeline simulation
            val aggregatedContext = pathNodes.joinToString("\n") { it.textContent }
            val dataHandler = DataHandler()

            // Inside your endpoint resource...
            val targetModelType = ModelType.CUSTOMS
            val payload = dataHandler.parseData(
                prompt = "Context:\n$aggregatedContext\n\nQuestion: who performs audits",
                config = GenerationConfig(),
                model = "llama3.2:1b",
                modelip = targetModelType
            )

            val ollamaHandler = OllamaHandler()
            val responseText = kotlinx.coroutines.runBlocking {
                val ioResponse = ollamaHandler.generateResponse(payload)
                ioResponse?.bodyAsText() ?: "Failed to get response from Ollama"
            }

            return Response.ok(
                mapOf(
                    "status" to "Success",
                    "chunksSeeded" to chunks.size,
                    "stepsTraversed" to pathNodes.size,
                    "path" to pathNodes.map { mapOf("id" to it.chunkId, "text" to it.textContent) },
                    "llmReply" to responseText
                )
            ).build()

        } catch (e: Exception) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message, "stack" to e.stackTraceToString()))
                .build()
        }
    }
}
