package piu.controllers

import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import org.piu.services.UserService
import piu.DTO.UserRoleAssignedRequest
import piu.DTO.toDTO
import piu.services.UserRoleService
import piu.services.UserRolesAssignedService

@Path("/api")
class UserRolesAssignedResource {
    @Inject
    lateinit var jwt: JsonWebToken

    @Inject
    lateinit var userRoleService: UserRoleService

    @Inject
    lateinit var userService: UserService




    @Inject
    lateinit var userRolesAssignedService: UserRolesAssignedService

    @POST
    @Path("/user-roles-assigned/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Transactional
    fun assignUserRole(request: UserRoleAssignedRequest): Response{
        userRolesAssignedService.assignRoles(request.userId,request.roleIds)
        return Response.status(Response.Status.CREATED).build()

    }


@GET
@RolesAllowed("admin")
@Path("/user/{userId}/roles")
fun getUserRoles(@PathParam("userId") userId: Long): Response {

   // val allRoles = userRoleService.findAll()
    val assignedRoleIds = userRolesAssignedService.findRolesByUserId(userId)
    val dtos = assignedRoleIds.map { it.toDTO() }
    return Response.ok(dtos).build()
}

}