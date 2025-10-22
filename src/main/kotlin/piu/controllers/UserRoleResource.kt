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
import piu.DTO.UserRolePermissionRequest
import piu.DTO.UserRoleRequest
import piu.DTO.toDTO
import piu.models.UserRole
import piu.services.UserPermissionService
import piu.services.UserRoleService
import piu.services.UserRolesAssignedService
import java.time.LocalDateTime


@Path("/api")
class UserRoleResource {
    @Inject
    lateinit var userRoleService: UserRoleService

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var userRolesAssignedService: UserRolesAssignedService

    @Inject
    lateinit var userPermissionService: UserPermissionService


    @Inject
    lateinit var jwt: JsonWebToken

    @GET
    @Path("/user-roles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional // Make sure the session is open while mapping
    fun findAll(): Response {
        val userRoles = userRoleService.findAll()
        val dtos = userRoles.map { it.toDTO() }
        return Response.ok(dtos).build()
    }

    @GET
    @Path("/user-roles/role/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)

    fun findRole(@PathParam("id") id: Long): Response? {
        val userRole = userRoleService.findById(id)
        val dto = userRole?.toDTO()
        return Response.ok(dto).build()

    }

    @POST
    @Path("/user-roles/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Transactional
    fun saveUserRole(request: UserRoleRequest): Response{

        val userId = (jwt.getClaim<Any>("email") as? String)?.toString()
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "Invalid user token")).build()
        }
        val user = userService.findByEmail(userId)

            ?: return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "User not found")).build()

        val userRole = UserRole(
            name = request.name,
            description = request.description,
            createdAt = LocalDateTime.now()
            )
        userRoleService.saveUserRole(userRole)
        return Response.status(Response.Status.CREATED).build()

    }


    @POST
    @Path("/user-roles/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Transactional
    fun saveUserRoleAndPermission(request: UserRolePermissionRequest): Response{

        val userId = (jwt.getClaim<Any>("email") as? String)?.toString()
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "Invalid user token")).build()
        }
        val user = userService.findByEmail(userId)

            ?: return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "User not found")).build()

        val userRole = UserRole(
            name = request.name,
            description = request.description,
            createdAt = LocalDateTime.now()
        )
        userRoleService.saveUserRole(userRole)

        userPermissionService.attachPermissionsRoles(userRole.id,request.permissionIds)

        return Response.status(Response.Status.CREATED).build()

    }



    @PUT
    @Path("/user-roles/edit/{id}")
    @RolesAllowed("admin" )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun updateUserRole(
        @PathParam("id") id: Long,
        request: UserRoleRequest
    ): Response {
        val userRole = userRoleService.findById(id) ?: return Response.status(Response.Status.NOT_FOUND)
                .entity("User role with id $id not found").build()

        userRole.name = request.name
        userRole.description = request.description
        userRole.updatedAt = LocalDateTime.now()

        userRoleService.saveUserRole(userRole)
        return Response.status(Response.Status.OK).build()

    }


    @DELETE
    @Path("/user-roles/{urid}")
    @RolesAllowed("admin" )
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun deleteUserRole(@PathParam("urid") urid: Long): Response {

        val userRole = userRoleService.findById(urid)
            ?: return Response.status(Response.Status.NOT_FOUND).entity("User role not found").build()

        userRoleService.deleteUserRolePermanently(urid)

        return Response.status(Response.Status.NO_CONTENT).build()
    }



}