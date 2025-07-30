package piu.models

data class UserRoleRequest(
    val admin: Boolean,
    val issueType: String?

)
