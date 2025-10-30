package piu.services

import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import piu.models.UserRole
import piu.repositories.UserRoleRepository
import java.time.LocalDateTime

@ApplicationScoped
class UserRoleService {
@Inject
lateinit var userRoleRepository: UserRoleRepository

    fun findAll(): List<UserRole>{
            return userRoleRepository.list("active = true",Sort.ascending("name"))
    }

    fun saveUserRole(userRole: UserRole){
        userRoleRepository.persist(userRole)
    }

    fun findById(id: Long?): UserRole? = userRoleRepository.findById(id)

    fun deleteUserRolePermanently(id: Long){
        userRoleRepository.deleteById(id)
    }

    fun findByName(name: String): UserRole{
        return userRoleRepository.findByName(name)
    }

    fun findOrCreateUserRole(): UserRole{
        val existingRole = findByName("User")
        if(existingRole != null) return existingRole
              val userRole = UserRole(
            name = "User",
            description = "General User",
            createdAt = LocalDateTime.now()
        )
        userRoleRepository.persist(userRole)
        return userRole
    }



}