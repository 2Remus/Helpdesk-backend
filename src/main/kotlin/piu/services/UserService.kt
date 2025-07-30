package org.piu.services

import io.quarkus.panache.common.Parameters
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.piu.models.SystemUser
import org.piu.repositories.UserRepository

@ApplicationScoped
@Transactional
class UserService {
    @Inject
    lateinit var userRepository: UserRepository

    fun findAll(): List<SystemUser>{
        return userRepository.listAll()
    }

    fun findById(id: Long): SystemUser?{
        return userRepository.findById(id)

    }

    fun findByEmail(email: String): SystemUser?{
        return userRepository.findByEmail(email)
    }


   /* fun updateUser(systemUser: SystemUser){
        userRepository.update(
            "email = '${systemUser.email}'"+
                    "where id = ${systemUser.id}"
        )

    }*/
    fun updateUser(systemUser: SystemUser) {
        userRepository.update(
            "email = :email where id = :id",
            Parameters.with("email", systemUser.email).and("id", systemUser.id)
        )
    }


    fun deleteUser(id: Long){
        userRepository.deleteById(id)
    }


    fun findByType(type: String): SystemUser?{
        return userRepository.find("isAdmin = true and issueType=?1",type).firstResult<SystemUser>()
    }

    fun save(user: SystemUser) {
        userRepository.persist(user)
    }


}