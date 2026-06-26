package piu.DTO

data class UserRoleAssignedRequest(

    val userId: Long,
    val roleIds: List<Long>
)
