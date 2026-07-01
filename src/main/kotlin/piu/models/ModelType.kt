package piu.models

import io.github.cdimascio.dotenv.Dotenv
import kotlinx.serialization.Serializable

object AppConfig {
    val dotenv: Dotenv by lazy {
        // Gets the absolute root path where you ran your gradle/quarkus command
        val projectRoot = System.getProperty("user.dir")

        Dotenv.configure()
            .directory(projectRoot) // Forces the lookup to use the absolute root path
            .filename(".env")
            .ignoreIfMalformed()
            .ignoreIfMissing() // Gracefully continues if the file isn't present
            .load()
    }
}

@Serializable
enum class ModelType(val testEndpoint: String) {
    // 1. Pass your local testing URLs directly into the constructor
    CUSTOMS("http://localhost:11434"),
    IRD("http://localhost:11435");

    val endpoint: String
        get() = when (this) {
            // 2. Try loading from .env first; fallback to your test endpoint string if it's missing
            CUSTOMS -> AppConfig.dotenv["CUSTOMS_AGENT_ENDPOINT"] ?: testEndpoint
            IRD -> AppConfig.dotenv["IRD_AGENT_ENDPOINT"] ?: testEndpoint
        }
}
