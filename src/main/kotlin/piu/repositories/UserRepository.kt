package org.piu.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import org.piu.models.SystemUser

@ApplicationScoped
class UserRepository: PanacheRepository<SystemUser>  {
    override fun findById(id: Long): SystemUser? = find("id",id).firstResult<SystemUser>()

    fun findByEmail(email: String?): SystemUser? {
        return find("email",email).firstResult<SystemUser>()
    }

    fun findByIssueType(type: String): SystemUser? {
        return find("type",type).firstResult<SystemUser>()
    }

    fun findByName(name: String?): SystemUser? {
        return find("name",name).firstResult<SystemUser>()
    }


}