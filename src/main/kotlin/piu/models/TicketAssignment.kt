package piu.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.piu.models.SystemUser
import org.piu.models.Ticket
import java.time.LocalDateTime

@Entity
data class TicketAssignment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name="created_at",nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name="updated_at" , nullable = true)
    var updatedAt: LocalDateTime? = null,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(nullable = false)
    var current: Boolean = true,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    var ticket: Ticket? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignedUser_id", nullable = false)
    var assignedUser: SystemUser? = null,


)
