package piu.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import piu.models.UserPermission

@ApplicationScoped
class UserPermissionRepository: PanacheRepository<UserPermission> {

    fun findByRoleId(roleId: Long): List<UserPermission>{
        return find("userRole.id",roleId).list<UserPermission>()
    }
}