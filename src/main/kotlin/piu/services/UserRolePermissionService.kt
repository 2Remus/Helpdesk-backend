package piu.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import piu.models.UserPermission
import piu.models.UserRole
import piu.models.UserRolePermission
import piu.repositories.UserRolePermissionRepository
import java.time.LocalDateTime

@ApplicationScoped
class UserRolePermissionService {
    @Inject
    lateinit var userRolePermissionRepository: UserRolePermissionRepository

    @Inject
    lateinit var userPermissionService: UserPermissionService

    fun findByRoleId(roleId: Long): List<UserRolePermission>{
       return userRolePermissionRepository.findByRoleId(roleId)
    }

    fun saveRolePermission(userRolePermission: UserRolePermission){
        userRolePermissionRepository.persist(userRolePermission)
    }


    fun deleteRolePermissionPermanently(id: Long){
        userRolePermissionRepository.deleteById(id)
    }

    @Transactional
    fun attachPermissionsToRole(userRole: UserRole, permissionIds: List<Long>) {

        userRolePermissionRepository.delete("userRole.id", userRole.id)
        // Add new assignments
        for (permissionId in permissionIds) {
            val permission = userPermissionService.findById(permissionId)
            val rolePermission = UserRolePermission(
            userRole = userRole,
            userPermission  = permission,
            createdAt = LocalDateTime.now()
            )
            userRolePermissionRepository.persist(rolePermission)
        }
    }
}