package piu.DTO

import piu.models.UserPermission

data class UserPermissionDTO(
    val id: Long?,
    val permission: String?,
    val description: String?,
 //   val userRole: String?,
)

fun UserPermission.toDTO(): UserPermissionResponseDTO  {
   return UserPermissionResponseDTO(
        id = this.id,
        permission = this.permission,
        description = this.description,
     /*   userRole = this.userRole?.let { role ->
            UserRoleDTO(
            id = role.id,
            name = role.name,
            description = role.description
        ) }*/
    )

}

