package piu.DTO

data class UserPermissionResponseDTO(

    val id: Long?,
    val permission: String?,
    val description: String?,
    val userRole: UserRoleDTO?
)
