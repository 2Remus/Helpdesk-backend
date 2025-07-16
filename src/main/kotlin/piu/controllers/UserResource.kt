package org.piu.controllers

import io.quarkus.elytron.security.common.BcryptUtil
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.piu.models.SystemUser
import org.piu.services.UserService
import piu.models.MessageResponseDTO
import piu.models.SystemUserDTO
import piu.models.SystemUserResponseDTO
import piu.models.toDTO
import java.time.LocalDateTime

@Path("/api")
class UserResource {
    @Inject
    lateinit var userService: UserService


    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    fun findAll(): Response{
            val users = userService.findAll()
            val userdtos = users.map { it.toDTO() }
        return Response.ok(userdtos).build()
    }

    @POST
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
        user.isAdmin = dto.isAdmin
        user.hashedPassword  = newBcrypt
        user.createdAt = LocalDateTime.now()
        user.issueType = dto.issueType

        userService.save(user)
        // Map to DTO before returning
       val userResponseDTO = SystemUserResponseDTO(
           id = user.id!!,
           email = user.email,
           issueType = user.issueType,
           isAdmin = user.isAdmin

       )
       return Response.status(Response.Status.CREATED).entity(userResponseDTO).build()
    }


    @DELETE
    @Path("/users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun deleteUser(@PathParam("userId") userId: Long): Response {
        val user = userService.findById(userId)
            ?: return Response.status(Response.Status.NOT_FOUND).entity("User not found").build()

        userService.deleteUser(userId)

        return Response.status(Response.Status.NO_CONTENT).build()
    }


}