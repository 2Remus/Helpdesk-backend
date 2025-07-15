package piu.models

import java.time.LocalDateTime

data class MessageResponseDTO(
    val id: Long,
    val content: String?,
    val createdAt: LocalDateTime,
    val ticketId: Long,
    val ticketSubject: String?,
    val sender: String?

)
