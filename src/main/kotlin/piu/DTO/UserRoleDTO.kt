package piu.DTO

import piu.models.IssueType
import piu.models.UserRole

data class UserRoleDTO(
    val id: Long?,
    val name: String?,
    val description: String?,
)

fun UserRole.toDTO(): UserRoleDTO = UserRoleDTO(
    id = this.id,
    name = this.name,
    description = this.description,
)
