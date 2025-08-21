package org.piu.models

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import piu.models.TicketAssignment
import java.time.LocalDateTime

@Entity
@Table(name = "tickets")
data class Ticket(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 100)
    var subject: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String? = null,

    @Column(nullable = false, length = 20)
    var status: String = "Open",

    @Column(nullable = false, length = 20)
    var priority: String = "Low",

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = true)
    var updatedAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var systemUser: SystemUser? = null,

    @OneToMany(mappedBy = "ticket", cascade = [CascadeType.ALL], orphanRemoval = true)
    var messages: List<Message> = mutableListOf(),

    @Column(nullable = true, length = 20)
    var assignedTo: String = "",

    @OneToMany(mappedBy = "ticket", cascade = [CascadeType.ALL], orphanRemoval = true)
    var ticketAssignments: List<TicketAssignment> = mutableListOf(),




    )
