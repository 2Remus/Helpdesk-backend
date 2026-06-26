package piu.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import piu.models.UserPermission
import piu.models.UserRolePermission

@ApplicationScoped
class UserRolePermissionRepository: PanacheRepository<UserRolePermission> {

    fun findByRoleId(roleId: Long): List<UserRolePermission>{
        return find("userRole.id",roleId).list<UserRolePermission>()
    }
}