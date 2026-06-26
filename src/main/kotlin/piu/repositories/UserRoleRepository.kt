package piu.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import piu.models.UserRole

@ApplicationScoped
class UserRoleRepository : PanacheRepository<UserRole>{

    fun findByName(name: String): UserRole{
        return find("name",name).firstResult<UserRole>()
    }


    fun deleteAllRolePermissions(roleId: Long?) {
        delete("id", roleId)
    }
}