package piu.utils

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonObject

class OllamaHandler {
    private val client = HttpClient(CIO)
    suspend fun generateResponse(payload: JsonElement): HttpResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse = client.post(payload.jsonObject["modelip"].toString()) {
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