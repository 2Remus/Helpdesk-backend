package piu.models

import java.time.LocalDateTime

data class TicketResponseDTO(
    val id: Long,
    val subject: String,
    val description: String,
    val priority: String,
    val status: String,
    val createdAt: LocalDateTime,
    val institutionId: Long,
    val institutionName: String,
    val reporter: SystemUserResponseDTO
)
