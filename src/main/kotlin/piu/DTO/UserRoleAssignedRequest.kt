package piu.DTO

data class UserRoleAssignedRequest(

   /* var systemUser: String,
    var userRole: String,*/
    val userId: Long,
    val roleIds: List<Long>
)
