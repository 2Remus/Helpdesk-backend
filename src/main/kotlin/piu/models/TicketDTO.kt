package org.piu.models

import java.time.LocalDateTime

data class TicketDTO(
    val id: Long,
    val subject: String?,
    val description: String?,
    val status: String,
    val priority: String,
    val createdAt: LocalDateTime,
    val systemUserEmail: String?,
    val assignedTo: String?,
)

fun Ticket.toDTO(): TicketDTO = TicketDTO(
    id = this.id ?: 0L,
    subject = this.subject,
    description = this.description,
    status = this.status,
    priority = this.priority,
    createdAt = this.createdAt,
    systemUserEmail = this.systemUser?.email,
    assignedTo =  this.assignedTo
)
