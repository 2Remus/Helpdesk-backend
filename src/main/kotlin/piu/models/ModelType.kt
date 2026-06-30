package piu.models
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.serialization.Serializable

object AppConfig {
    val dotenv: Dotenv by lazy {
        Dotenv.configure()
            .directory("./") // Looks for .env in root directory
            .filename(".env")
            .load()
    }
}


@Serializable
enum class ModelType {
    CUSTOMS,
    IRD;


    val endpoint: String
        get() = when (this) {
            CUSTOMS -> AppConfig.dotenv["CUSTOMS_AGENT_ENDPOINT"]
            IRD -> AppConfig.dotenv["IRD_AGENT_ENDPOINT"]
        }
}