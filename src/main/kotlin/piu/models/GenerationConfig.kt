package piu.models

import kotlinx.serialization.Serializable

@Serializable
data class GenerationConfig(
    var temp: Float = 0.5f,
    var topP: Float = 0.9f,
    var repetitionPenalty: Float = 1.1f,
    var maxNewTokens: Int = 128,
    var doSample: Boolean = true
)
