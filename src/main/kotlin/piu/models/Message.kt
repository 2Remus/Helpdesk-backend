package org.piu.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import piu.DTO.MessageResponseDTO
import java.time.LocalDateTime

@Entity
@Table(name = "messages")
data class Message(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String? = null,

    @Column(nullable = false, length = 120)
    var sender: String? = null,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    var ticket: Ticket? = null
)

fun Message.toDTO(): MessageResponseDTO = MessageResponseDTO(
    id = this.id!!,
    content = this.content,
    createdAt = this.createdAt,
    ticketId = this.ticket?.id!!,
    ticketSubject = this.ticket?.subject,
    sender = this.sender
)
