
package piu.models
import kotlinx.serialization.Serializable

@Serializable
data class GenerationConfig(
    var temp: Float,
    var topP : Float,
    var repetitionPenalty : Float,
    var maxNewTokens: Int,
    var doSample: Boolean
    )