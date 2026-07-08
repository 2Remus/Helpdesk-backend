package piu.models

import kotlinx.serialization.Serializable

@Serializable
data class GenerationConfig(
    var temp: Float = 0.0f,
    var topP: Float = 1.0f,
    var repetitionPenalty: Float = 1.0f,
    var maxNewTokens: Int = 120,
    var doSample: Boolean = false
)
