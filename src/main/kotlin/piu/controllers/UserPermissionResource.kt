package piu.controllers

import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import org.piu.services.UserService
import piu.DTO.UserPermissionRequest
import piu.DTO.toDTO
import piu.models.UserPermission
import piu.services.UserPermissionService
import piu.services.UserRoleService
import java.time.LocalDateTime

@Path("/api")
class UserPermissionResource {

    @Inject
    lateinit var jwt: JsonWebToken
    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var userPermissionService: UserPermissionService

    @Inject
    lateinit var userRoleService: UserRoleService
    @GET
    @Path("/user-permissions")

    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional // Make sure the session is open while mapping
    fun findAll(): Response {
        val userPermissions = userPermissionService.findAll()
        val dtos = userPermissions.map { it.toDTO() }
        return Response.ok(dtos).build()
    }

    @GET
    @Path("/user-permissions/role/{id}")

    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional // Make sure the session is open while mapping
    fun findPermissionsByRole(@PathParam("id") id: Long): Response {
        val userPermissions = userPermissionService.findByRoleId(id)
        val dtos = userPermissions.map { it.toDTO() }
        return Response.ok(dtos).build()
    }

    @POST
    @Path("/user-permissions/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Transactional
    fun saveUserPermission(request: UserPermissionRequest): Response{

        val userId = (jwt.getClaim<Any>("email") as? String)?.toString()
        if (userId == null) {
            println("Invalid token")
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "Invalid user token")).build()
        }
        val user = userService.findByEmail(userId)

            ?: return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "User not found")).build()

        val userPermission = UserPermission(
            permission = request.permission,
            description = request.description,
            createdAt = LocalDateTime.now()
        )
        userPermission.userRole = if (request.userRole.isNotBlank()) {
            userRoleService.findByName(request.userRole)
        } else {
            null
        }



        userPermissionService.saveUserPermission(userPermission)
        return Response.status(Response.Status.CREATED).build()

    }




    @PUT
    @Path("/user-permissions/edit/{id}")
    @RolesAllowed("admin" )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun updateUserPermission(
        @PathParam("id") id: Long,
        request: UserPermissionRequest
    ): Response {
        val userPermission = userPermissionService.findById(id) ?: return Response.status(Response.Status.NOT_FOUND)
            .entity("User permission with id $id not found").build()

        userPermission.permission = request.permission
        userPermission.description = request.description
        userPermission.updatedAt = LocalDateTime.now()

        userPermissionService.saveUserPermission(userPermission)
        return Response.status(Response.Status.OK).build()

    }


    @DELETE
    @Path("/user-permissions/{upid}")
    @RolesAllowed("admin" )
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun deleteUserPermission(@PathParam("upid") upid: Long): Response {
        val userPermission = userPermissionService.findById(upid)
            ?: return Response.status(Response.Status.NOT_FOUND).entity("User Permission not found").build()

        userPermissionService.deleteUserPermissionPermanently(upid)

        return Response.status(Response.Status.NO_CONTENT).build()
    }
}