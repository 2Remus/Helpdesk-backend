package piu.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
data class UserPermission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true)
    var permission: String? ="",
    var description: String? = "",
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
    var active: Boolean? = true,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userRole_id")
    var userRole: UserRole ?= null,
)
