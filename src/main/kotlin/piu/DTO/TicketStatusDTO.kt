package piu.DTO

import piu.models.IssueType
import piu.models.TicketStatus

data class TicketStatusDTO(
    val id: Long?,
    val name: String?,
    val description: String?,

)

fun TicketStatus.toDTO(): TicketStatusDTO = TicketStatusDTO(
    id = this.id,
    name = this.name,
    description = this.description,
)
