package piu.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import piu.models.GenerationConfig;
import piu.models.PraseResponse
import piu.models.ModelType

class DataHandler {
    public fun parseData(prompt: String, config: GenerationConfig, model: String, modeltype: ModelType )  : JsonElement{
        val data : PraseResponse = PraseResponse(model, prompt, stream = true, options = config, modeltype)

        val json = Json { ignoreUnknownKeys = true }

        val jsonResp: JsonElement = json.encodeToJsonElement(data)
        return jsonResp;
    }
}