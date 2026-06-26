package piu.DTO

import org.piu.models.Ticket
import org.piu.models.TicketDTO
import piu.models.UserRolesAssigned

data class UserRolesAssignedDTO(

    val id: Long?,
    val userId: Long?,
    val roleId: Long?,


)
fun UserRolesAssigned.toDTO(): UserRolesAssignedDTO = UserRolesAssignedDTO(
    id = this.id ?: 0L,
    userId = this.systemUser?.id,
    roleId = this.userRole?.id,

)
