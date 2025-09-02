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
data class UserSignature(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "systemUser_id")
    var systemUser: SystemUser ?= null,

    @Column(name = "signature", columnDefinition = "bytea")
    var signature: ByteArray? = null,

    @Column(nullable = false)
    var active: Boolean = false,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true)
    var updatedAt: LocalDateTime? = null,
)
