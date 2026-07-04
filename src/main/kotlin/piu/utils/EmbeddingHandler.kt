package piu.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
// 💡 Uncomment this line—this provides the json(...) extension function for Ktor!
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class OllamaEmbedRequest(
    val model: String,
    val input: List<String>
)

@Serializable
data class OllamaEmbedResponse(
    val model: String,
    val embeddings: List<FloatArray>
)

class LocalEmbeddingProvider(private val modelName: String = "mxbai-embed-large") {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            // 💡 Pass an explicit configured Json block inside the function
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getEmbedding(texts: List<String>): List<FloatArray> {
        if (texts.isEmpty()) return emptyList()

        val response: OllamaEmbedResponse = client.post("http://localhost:11434/api/embed") {
            contentType(ContentType.Application.Json)
            setBody(OllamaEmbedRequest(model = modelName, input = texts)) // Directly passes the whole list
        }.body()

        return response.embeddings
    }

    fun close() = client.close()
}
