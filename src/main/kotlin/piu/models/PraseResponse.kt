package piu.models

import kotlinx.serialization.Serializable

@Serializable
data class PraseResponse(
    var model : String,
    var prompt : String,
    var stream : Boolean,
    var options: GenerationConfig,
    var modelIp: ModelType
)