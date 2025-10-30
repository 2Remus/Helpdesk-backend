package piu.services

import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import piu.models.UserPermission
import piu.models.UserRole
import piu.repositories.UserPermissionRepository
import piu.repositories.UserRoleRepository
import java.time.LocalDateTime


@ApplicationScoped
class UserPermissionService {
    @Inject
    lateinit var userPermissionRepository: UserPermissionRepository

    @Inject
    lateinit var userRoleRepository: UserRoleRepository

    @Inject
    lateinit var userRolesAssignedService: UserRolesAssignedService

    fun findAll(): List<UserPermission>{
        return userPermissionRepository.list("active = true",Sort.ascending("permission"))
    }

    fun saveUserPermission(userPermission: UserPermission){
        userPermissionRepository.persist(userPermission)
    }

    fun findById(id: Long): UserPermission? = userPermissionRepository.findById(id)

    fun findByRoleId(roleId: Long): List<UserPermission>{
        return userPermissionRepository.findByRoleId(roleId)
    }

    fun deleteUserPermissionPermanently(id: Long){
        userPermissionRepository.deleteById(id)
    }


    @Transactional
    fun attachPermissionsRoles(userRole: UserRole, permissionIds: List<Long>) {
       /* val userRole = userRoleRepository.findById(roleId)
            ?: throw IllegalArgumentException("Role not found")*/
        // Delete existing assignments
      // deleteAllRolePermissions(userRole.id)
        userPermissionRepository.delete("userRole.id", userRole.id)
        userRolesAssignedService
        // Add new assignments
        for (permissionId in permissionIds) {
            println("Permission individual id $permissionId")
            val permission = userPermissionRepository.findById(permissionId)
            println("Permission  $permission")

         //   permission.userRole = userRole
            permission.updatedAt = LocalDateTime.now()

            userPermissionRepository.persist(permission)
        }
    }


    fun deleteAllRolePermissions(roleId: Long?){
        userRoleRepository.deleteAllRolePermissions(roleId)
    }
}