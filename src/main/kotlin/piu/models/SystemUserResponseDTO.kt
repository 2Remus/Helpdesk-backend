package piu.models

data class SystemUserResponseDTO(
    val id: Long,
    val email: String?,
    val isAdmin: Boolean,
    val issueType: String?
)


