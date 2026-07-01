package piu.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import piu.models.GenerationConfig
import piu.models.PraseResponse
import piu.models.ModelType

class DataHandler {
    fun parseData(prompt: String, config: GenerationConfig, model: String, modelip: ModelType): JsonElement {

        // Extract the actual IP/URL string property from the ModelType instance
        val endpointUrl = modelip.endpoint

        // Pass the resolved string URL down into your response data structure
        val data: PraseResponse = PraseResponse(
            model = model,
            prompt = prompt,
            stream = true,
            options = config,
            modelip = endpointUrl // Make sure PraseResponse expects a String here
        )

        val json = Json { ignoreUnknownKeys = true }
        val jsonResp: JsonElement = json.encodeToJsonElement(data)
        return jsonResp
    }
}
