package piu.models

import org.piu.models.SystemUser
import kotlin.String

data class SystemUserDTO(
    val id: Long?,
    val name: String,
    val email: String?,
    val password: String,
    val admin: Boolean,
    val active: Boolean,
    val issueType: String?,
    val institutionId: Long?


)
fun SystemUser.toDTO(): SystemUserResponseDTO {
    return SystemUserResponseDTO(
        id = this.id!!,
        name = this.name,
        email = this.email,
        admin = this.admin,
        active = this.active,
        issueType = this.issueType,
        institutionId = this.institution?.id
        )
}

