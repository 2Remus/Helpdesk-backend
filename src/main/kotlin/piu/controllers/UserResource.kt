package org.piu.controllers

import io.quarkus.elytron.security.common.BcryptUtil
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.PATCH
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.piu.models.SystemUser
import org.piu.services.UserService
import piu.models.InstitutionRequest
import piu.models.SystemUserDTO
import piu.models.SystemUserResponseDTO
import piu.models.UserRequest
import piu.models.UserRoleRequest
import piu.models.toDTO
import java.time.LocalDateTime

@Path("/api")
class UserResource {
    @Inject
    lateinit var userService: UserService


    @GET
    @Path("/users")
    @RolesAllowed("admin" )
    @Produces(MediaType.APPLICATION_JSON)
    fun findAll(): Response{
            val users = userService.findAll()
            val userdtos = users.map { it.toDTO() }
        return Response.ok(userdtos).build()
    }

    @POST
    @RolesAllowed("admin" )
    @Path("/users/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun createUser(
        @PathParam("ticketId") ticketId: Long,
        dto: SystemUserDTO
    ): Response {

        val newBcrypt = BcryptUtil.bcryptHash(dto.password)

        val user = SystemUser()
        user.email = dto.email
        user.name = dto.name
        user.admin = dto.admin
        user.hashedPassword  = newBcrypt
        user.createdAt = LocalDateTime.now()
        user.issueType = dto.issueType

        userService.save(user)
        // Map to DTO before returning
       val userResponseDTO = SystemUserResponseDTO(
           id = user.id!!,
           name = user.name,
           email = user.email,
           issueType = user.issueType,
           admin = user.admin,
           institutionId = user.institution?.id

       )
       return Response.status(Response.Status.CREATED).entity(userResponseDTO).build()
    }


    @DELETE
    @Path("/users/{userId}")
    @RolesAllowed("user" )
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun deleteUser(@PathParam("userId") userId: Long): Response {
        val user = userService.findById(userId)
            ?: return Response.status(Response.Status.NOT_FOUND).entity("User not found").build()

        userService.deleteUser(userId)

        return Response.status(Response.Status.NO_CONTENT).build()
    }





    @GET
    @Path("/users/{usId}")
    @RolesAllowed("admin" )
    @Produces(MediaType.APPLICATION_JSON)
    fun getById(@PathParam("usId") id: Long): Response {
        val user = userService.findById(id)
        return if (user != null) {
            val dto = user.toDTO() // cleaner with extension function
            Response.ok(dto).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @PUT
    @Path("/users/edit/{id}")
    @RolesAllowed("admin" )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun updateUser(
        @PathParam("id") id: Long,
        request: UserRequest
    ): Response {
        val user = userService.findById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity("User with id $id not found").build()

        user.name = request.name
        user.email = request.email
        user.admin = request.admin
        user.issueType = request.issueType
        user.updatedAt = LocalDateTime.now()

        userService.updateUser(user)
        return Response.status(Response.Status.OK).build()

    }



    @PUT
    @Path("/users/edit/role/{id}")
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun updateUserRole(
        @PathParam("id") id: Long,
        request: UserRoleRequest
    ): Response {
        val user = userService.findById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity("User with id $id not found").build()

        user.admin = request.admin
        user.issueType = request.issueType
        user.updatedAt = LocalDateTime.now()

        userService.save(user)

        return Response.ok(mapOf("message" to "User updated successfully")).build()
    }



}