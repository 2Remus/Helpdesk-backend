package piu.services

import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import piu.models.UserRole
import piu.repositories.UserRoleRepository

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

    fun findById(id: Long): UserRole? = userRoleRepository.findById(id)

    fun deleteUserRolePermanently(id: Long){
        userRoleRepository.deleteById(id)
    }

    fun findByName(name: String): UserRole{
        return userRoleRepository.findByName(name)
    }



}