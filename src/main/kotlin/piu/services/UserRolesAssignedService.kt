package piu.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.piu.models.SystemUser
import org.piu.services.UserService
import piu.models.UserRolesAssigned
import piu.repositories.UserRolesAssignedRepository
import java.time.LocalDateTime


@ApplicationScoped
class UserRolesAssignedService {

    @Inject
    lateinit var userRolesAssignedRepository: UserRolesAssignedRepository
    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var userRolesAssignedService: UserRolesAssignedService

    @Inject
    lateinit var userRoleService: UserRoleService


    fun findRolesByUser(systemUser: SystemUser): List<UserRolesAssigned>{
        return userRolesAssignedRepository.findRolesByUser(systemUser)
    }
    fun findRolesByUserId(usid: Long?): List<UserRolesAssigned>{
        return userRolesAssignedRepository.findRolesByUserId(usid)
    }



    fun saveUserAssignment(userRolesAssigned: UserRolesAssigned){
        userRolesAssignedRepository.persist(userRolesAssigned)
    }

    fun deleteAllUserAssignments(usid: Long?){
        userRolesAssignedRepository.deleteAllUserAssignments(usid)
    }




    @Transactional
    fun assignRoles(userId: Long, roleIds: List<Long>) {
        val user = userService.findById(userId)
            ?: throw IllegalArgumentException("User not found")
        // Delete existing assignments
        deleteAllUserAssignments(userId)

        // Add new assignments
        for (roleId in roleIds) {
            val role = userRoleService.findById(roleId)
                ?: continue
            val assigned = UserRolesAssigned(
                systemUser = user,
                userRole = role,
                createdAt = LocalDateTime.now()
            )
            userRolesAssignedService.saveUserAssignment(assigned)
        }
    }

    @Transactional
    fun assignRole(userId: Long?, roleId: Long?) {
        val user = userService.findById(userId)
            ?: throw IllegalArgumentException("User not found")
        // Delete existing assignments
        deleteAllUserAssignments(userId)

        // Add new assignments
            val role = userRoleService.findById(roleId)
            val assigned = UserRolesAssigned(
                systemUser = user,
                userRole = role,
                createdAt = LocalDateTime.now()
            )
            userRolesAssignedService.saveUserAssignment(assigned)

    }


}