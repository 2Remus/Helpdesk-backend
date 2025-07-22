package piu.models

import com.fasterxml.jackson.annotation.JsonProperty
import org.piu.models.SystemUser
import kotlin.String

data class SystemUserDTO(
    val id: Long,
    val name: String,
    val email: String?,
    val password: String,
    val admin: Boolean,
    val issueType: String?,
    val institutionId: Long?


)
fun SystemUser.toDTO(): SystemUserResponseDTO {
    return SystemUserResponseDTO(
        id = this.id!!,
        name = this.name,
        email = this.email,
        admin = this.admin,
        issueType = this.issueType,
        institutionId = this.institution?.id
        )
}
/*
fun toDTO(user: SystemUser): SystemUserDTO {
    return SystemUserDTO(
        id = user.id!!,
        name = user.name,
        email = user.email,
        isAdmin = user.isAdmin,
        issueType = user.issueType,
        password = "",
        institutionId = user.institution?.id
    )
}*/

