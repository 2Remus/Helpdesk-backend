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
import piu.models.Institution
import piu.models.SystemUserResponseDTO
import java.time.LocalDateTime

@Entity
data class SystemUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var name: String = "",
    @Column(nullable = false, unique = true, length = 120)
    var email: String? = null,

    @Column(nullable = false, length = 255)
    var hashedPassword: String? = null,

    @Column(nullable = false)
    var admin: Boolean = false,

    @Column(nullable = true, length = 255)
    var issueType: String? = null,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = true)
    var updatedAt: LocalDateTime? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    var institution: Institution ?= null,

    @Column(nullable = false)
    var active: Boolean = false,

    @Column
    var activationToken: String? = null,

    @OneToMany(mappedBy = "systemUser", cascade = [CascadeType.ALL], orphanRemoval = true)
    var tickets: List<Ticket> = mutableListOf(),

    @OneToMany(mappedBy = "assignedTo", cascade = [CascadeType.ALL], orphanRemoval = true)
    var assignedTickets: List<Ticket> = mutableListOf()
)
