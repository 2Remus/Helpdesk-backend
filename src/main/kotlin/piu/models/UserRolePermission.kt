package piu.models

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
data class UserRolePermission(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userRole_id")
    var userRole: UserRole? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userPermission_id")
    var userPermission: UserPermission? = null,
    var createdAt: LocalDateTime? = null,
)
