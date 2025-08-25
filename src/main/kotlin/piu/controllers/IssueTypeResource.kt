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
import org.piu.models.Ticket
import org.piu.services.UserService
import piu.DTO.IssueTypeRequest
import piu.DTO.toDTO
import piu.models.IssueType
import piu.models.UserRequest
import piu.services.IssueTypeService
import project.cardtp.models.TicketRequest
import java.time.LocalDateTime

@Path("/api")

class IssueTypeResource {
    @Inject
    lateinit var issueTypeService: IssueTypeService

    @Inject
    lateinit var jwt: JsonWebToken

    @Inject
    lateinit var userService: UserService

    @GET
    @Path("/issue-types")

    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional // Make sure the session is open while mapping
    fun findAll(): Response {
        val issueTypes = issueTypeService.findAllActive()
        val dtos = issueTypes.map { it.toDTO() }
        return Response.ok(dtos).build()
    }



    @POST
    @Path("/issue-types/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Transactional
    fun saveIssueType(request: IssueTypeRequest): Response{

        val userId = (jwt.getClaim<Any>("email") as? String)?.toString()
        if (userId == null) {
            println("Invalid token")
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "Invalid user token")).build()
        }
        val user = userService.findByEmail(userId)

            ?: return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "User not found")).build()

        val issueType = IssueType(
            name = request.name,
            description = request.description,


        )
        issueTypeService.saveIssueType(issueType)
        return Response.status(Response.Status.CREATED).build()

    }


    @DELETE
    @Path("/issue-types/{issueTypeId}")
    @RolesAllowed("admin" )
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun deleteIssueType(@PathParam("issueTypeId") issueTypeId: Long): Response {
        val issueType = issueTypeService.findById(issueTypeId)
            ?: return Response.status(Response.Status.NOT_FOUND).entity("Issue type not found").build()

        issueTypeService.deleteIssueTypePermanently(issueTypeId)

        return Response.status(Response.Status.NO_CONTENT).build()
    }


    @PUT
    @Path("/issue-types/edit/{id}")
    @RolesAllowed("admin" )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun updateIssueType(
        @PathParam("id") id: Long,
        request: IssueTypeRequest
    ): Response {
        val issueType = issueTypeService.findById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity("Issue type with id $id not found").build()

        issueType.name = request.name
        issueType.description = request.description
        issueType.updatedAt = LocalDateTime.now()

        issueTypeService.saveIssueType(issueType)
        return Response.status(Response.Status.OK).build()

    }
}