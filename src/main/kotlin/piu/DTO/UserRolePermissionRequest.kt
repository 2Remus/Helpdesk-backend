package piu.DTO

data class UserRolePermissionRequest(
    var name: String,
    var description: String,
    val permissionIds: List<Long>
)
