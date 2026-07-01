package piu.utils

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.quarkus.runtime.ShutdownEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import kotlinx.serialization.json.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@ApplicationScoped
class OllamaHandler {

    private val client = HttpClient(CIO)

    // Safely close Ktor threads during Quarkus live-reloads to prevent memory leaks
    fun onShutdown(@Observes event: ShutdownEvent) {
        client.close()
    }

    suspend fun generateResponse(payload: JsonElement): HttpResponse? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Extract raw string value safely without structural JSON quotes ("")
                val rawIp = payload.jsonObject["modelip"]?.jsonPrimitive?.content
                    ?: throw IllegalArgumentException("Missing 'modelip' in payload")

                // 2. Clean and format the base endpoint URL
                val cleanUrl = rawIp.removeSuffix("/")
                val targetEndpoint = if (cleanUrl.startsWith("http")) {
                    "$cleanUrl/api/generate"
                } else {
                    "http://$cleanUrl/api/generate"
                }

                // 3. Make the call using the idiomatic content-type builder
                val response: HttpResponse = client.post(targetEndpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(payload.toString())
                }
                response
            } catch (ex: Exception) {
                println("Error sending request to Ollama: ${ex.message}")
                null
            }
        }
    }
}
