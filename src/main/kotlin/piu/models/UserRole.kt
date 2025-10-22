package piu.models

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.time.LocalDateTime

@Entity
data class UserRole(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true)
    var name: String? ="",
    var description: String? = "",
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
    var active: Boolean? = true,

    @OneToMany(mappedBy = "userRole", cascade = [CascadeType.ALL], orphanRemoval = true,fetch = FetchType.LAZY)
    var userPermissions: List<UserPermission> = mutableListOf(),
)
