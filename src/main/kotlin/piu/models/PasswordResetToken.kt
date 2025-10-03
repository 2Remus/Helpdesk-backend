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
import java.time.LocalDateTime

@Entity
data class PasswordResetToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column
    var token: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "systemUser_id")
    var systemUser: SystemUser ?= null,

    @Column(nullable = true)
    var expiryDate: LocalDateTime? = null,

    )
