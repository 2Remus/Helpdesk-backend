package piu.DTO

import piu.models.UserRolePermission

data class UserRolePermissionDTO(

    val id: Long?,
    val userRole: String?,
    val userPermission: String?,
)

fun UserRolePermission.toDTO(): UserRolePermissionResponseDTO = UserRolePermissionResponseDTO(
    id = this.id,
    userRole =  this.userRole?.let { role ->
        UserRoleDTO(
            id = role.id,
            name = role.name,
            description = role.description
        ) },
    userPermission =  this.userPermission?.let { perm ->
        UserPermissionDTO(
            id = perm.id,
            permission = perm.permission,
            description = perm.description,

        ) },
)