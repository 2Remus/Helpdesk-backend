package org.piu.controllers

import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.piu.models.Ticket
import org.piu.models.toDTO
import org.piu.services.TicketService
import org.piu.services.UserService
import project.cardtp.models.TicketRequest
import java.time.LocalDateTime

@Path("/api")
class TicketResource {
    @Inject
    lateinit var ticketService: TicketService

    @Inject
    lateinit var userService: UserService


    @GET
    @Path("/tickets")
    @RolesAllowed("admin" )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional // Make sure the session is open while mapping
    fun findAll(): Response {
        val tickets = ticketService.findAll()
        val dtos = tickets.map { it.toDTO() }
        return Response.ok(dtos).build()
    }


    @POST
    @Path("/tickets/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin","user")
    @Transactional
    fun saveTicket(request: TicketRequest): Response{
        //  val userId = jwt.getClaim<Long>("id")
     //   val userId = jwt.getClaim<Any>("id").toString().toLong()
        val user = userService.findById(1)
        val issueType = request.type

        /*    val admin = userService.findByType(issueType) ?: return Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("detail" to "No admin available for issue type: $issueType"))
                    .build()*/

        val ticket = Ticket(
            subject = request.subject,
            description = request.description,
            status = "Submitted",
            priority = request.priority,
            createdAt = LocalDateTime.now(),
            systemUser = user

        )

        ticketService.saveTicket(ticket)
        return Response.status(Response.Status.CREATED).build()

    }





}