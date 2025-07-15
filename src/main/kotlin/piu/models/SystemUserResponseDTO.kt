package piu.models

data class SystemUserResponseDTO(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    val institutionId: Long?

)


