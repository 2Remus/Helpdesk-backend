package piu.services

import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import piu.models.UserPermission
import piu.repositories.UserPermissionRepository
import piu.repositories.UserRoleRepository
import java.time.LocalDateTime


@ApplicationScoped
class UserPermissionService {
    @Inject
    lateinit var userPermissionRepository: UserPermissionRepository

    @Inject
    lateinit var userRoleRepository: UserRoleRepository


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
    fun attachPermissionsRoles(roleId: Long?, permissionIds: List<Long>) {
        val userRole = userRoleRepository.findById(roleId)
            ?: throw IllegalArgumentException("Role not found")
        // Delete existing assignments
        deleteAllRolePermissions(roleId)

        // Add new assignments
        for (permissionId in permissionIds) {
            val role = userRoleRepository.findById(roleId)
                ?: continue
            val permission = userPermissionRepository.findById(permissionId)
            permission.userRole = role
            permission.updatedAt = LocalDateTime.now()

            userPermissionRepository.persist(permission)
        }
    }


    fun deleteAllRolePermissions(roleId: Long?){
        userRoleRepository.deleteAllRolePermissions(roleId)
    }
}