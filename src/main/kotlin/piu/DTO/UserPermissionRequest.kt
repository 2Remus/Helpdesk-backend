package piu.DTO

import java.time.LocalDateTime

data class UserPermissionRequest(
    var permission: String,
    var description: String,
   /* var userRole: String*/
)

data class MessageResponseDTO(
    val id: Long,
    val content: String?,
    val createdAt: LocalDateTime,
    val ticketId: Long,
    val ticketSubject: String?,
    val sender: String?

)