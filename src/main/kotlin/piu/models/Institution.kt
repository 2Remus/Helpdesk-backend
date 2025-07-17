package piu.models

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import org.piu.models.SystemUser
import java.time.LocalDateTime

@Entity
data class Institution(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String?= "",

    var address: String?= null,

    var email: String? = null,

    var phoneNumber: String? = null,

    var createdAt: LocalDateTime? = null,

    var updatedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "institution", cascade = [CascadeType.ALL], orphanRemoval = true)
    var users: List<SystemUser> = emptyList()
)
