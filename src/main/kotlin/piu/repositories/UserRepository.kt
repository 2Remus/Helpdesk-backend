package org.piu.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import org.piu.models.SystemUser

@ApplicationScoped
class UserRepository: PanacheRepository<SystemUser>  {
    /*override fun findById(id: Long): SystemUser? = find("id",id).firstResult<SystemUser>()
    */
/*
    fun findByEmail(email: String?): SystemUser? {
        return find("email",email).firstResult<SystemUser>()
    }*/
    fun findByEmail(email: String?): SystemUser? {
        return find("SELECT u FROM SystemUser u LEFT JOIN FETCH u.institution WHERE u.email = ?1", email)
            .firstResult()
    }

    fun findByIssueType(type: String): SystemUser? {
        return find("type",type).firstResult<SystemUser>()
    }

    fun findByName(name: String?): SystemUser? {
        return find("name",name).firstResult<SystemUser>()
    }

/*
    fun findAvailableUsers(): List<SystemUser> {
        return find("admin", true).list()
    }*/
/*
    fun findAvailableUsers(): List<SystemUser> {
        return find("SELECT u FROM SystemUser u WHERE u.admin = true and u.active = true LEFT JOIN FETCH u.institution ").list()
    }
*/


    // Fetch all admin users with their institution eagerly loaded
    fun findAdminUsers(): List<SystemUser> {
        return find(
            "SELECT u FROM SystemUser u LEFT JOIN FETCH u.institution WHERE u.admin = true"
        ).list()
    }

    // Fetch all available users (e.g., active admins)
    fun findAvailableUsers(): List<SystemUser> {
        return find(
            "SELECT u FROM SystemUser u LEFT JOIN FETCH u.institution WHERE u.admin = true AND u.active = true"
        ).list()
    }

    // Optional: fetch by ID with institution eagerly loaded
    fun findByIdWithInstitution(id: Long): SystemUser? {
        return find(
            "SELECT u FROM SystemUser u LEFT JOIN FETCH u.institution WHERE u.id = ?1",
            id
        ).firstResult()
    }
}