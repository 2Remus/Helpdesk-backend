package piu.DTO

data class UserRolePermissionResponseDTO(
    val id: Long?,
    val userPermission: UserPermissionDTO?,
    val userRole: UserRoleDTO?
)
