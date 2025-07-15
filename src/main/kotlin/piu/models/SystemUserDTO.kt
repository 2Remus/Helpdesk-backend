package piu.models

import org.piu.models.SystemUser
import kotlin.String

data class SystemUserDTO(
    val id: Long,
    val email: String?,
    val password: String,
    val isAdmin: Boolean,
    val issueType: String?


)
fun SystemUser.toDTO(): SystemUserResponseDTO {
    return SystemUserResponseDTO(
        id = this.id!!,
        email = this.email,
        isAdmin = this.isAdmin,
        issueType = this.issueType

        )
}
