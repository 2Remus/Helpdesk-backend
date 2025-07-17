package piu.models

data class SystemUserResponseDTO(
    val id: Long,
    val name: String?,
    val email: String?,
    val isAdmin: Boolean,
    val issueType: String?
)


