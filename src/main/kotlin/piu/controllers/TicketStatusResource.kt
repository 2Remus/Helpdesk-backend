package piu.controllers

import jakarta.annotation.security.RolesAllowed
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
import org.eclipse.microprofile.jwt.JsonWebToken
import org.piu.models.toDTO
import org.piu.services.UserService
import piu.DTO.IssueTypeRequest
import piu.DTO.TicketStatusRequest
import piu.DTO.toDTO
import piu.models.IssueType
import piu.models.TicketStatus
import piu.services.TicketStatusService

@Path("/api")
class TicketStatusResource {
    @Inject
    lateinit var jwt: JsonWebToken

    @Inject
    lateinit var ticketStatusService: TicketStatusService

    @Inject
    lateinit var userService: UserService


    @GET
    @Path("/ticket-statuses")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun findAll(): Response {
        val ticketStatus = ticketStatusService.findAll()
        val dtos = ticketStatus.map { it.toDTO() }
        return Response.ok(dtos).build()
    }





    @POST
    @Path("/ticket-status/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Transactional
    fun saveTicketStatus(request: TicketStatusRequest): Response{

        val userId = (jwt.getClaim<Any>("email") as? String)?.toString()
        if (userId == null) {
            println("Invalid token")
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "Invalid user token")).build()
        }
        val user = userService.findByEmail(userId)

            ?: return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "User not found")).build()

        val ticketStatus = TicketStatus(
            name = request.name,
            description = request.description,


            )
        ticketStatusService.saveTicketStatus(ticketStatus)
        return Response.status(Response.Status.CREATED).build()

    }


    @DELETE
    @Path("/ticket-status/{ticketStatusId}")
    @RolesAllowed("admin" )
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun deleteIssueType(@PathParam("ticketStatusId") ticketStatusId: Long): Response {
        val ticketStatus = ticketStatusService.findById(ticketStatusId)
            ?: return Response.status(Response.Status.NOT_FOUND).entity("Ticket status not found").build()

        ticketStatusService.deleteTicketStatusPermanently(ticketStatusId)

        return Response.status(Response.Status.NO_CONTENT).build()
    }

}