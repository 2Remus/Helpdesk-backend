package piu.models

data class SystemUserResponseDTO(
    val id: Long,
    val name: String?,
    val email: String?,
    val admin: Boolean,
    val issueType: String?,
    val institutionId: Long?
)


