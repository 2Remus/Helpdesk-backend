package org.piu.controllers

import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
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
import org.piu.models.toDTO
import org.piu.services.TicketService
import org.piu.services.UserService
import piu.models.PriorityUpdateRequest
import piu.models.StatusUpdateRequest
import piu.models.SystemUserResponseDTO
import piu.models.TicketResponseDTO
import project.cardtp.models.TicketRequest
import java.time.LocalDateTime

@Path("/api")
class TicketResource {
    @Inject
    lateinit var ticketService: TicketService

    @Inject
    lateinit var userService: UserService
    @Inject
    lateinit var jwt: JsonWebToken

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


    @GET
    @Path("/myTickets")
    @RolesAllowed("user","admin" )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional // Make sure the session is open while mapping
    fun myTickets(): Response {
        val email = (jwt.getClaim<Any>("email") as? String)?.toString()
        if (email == null) {
            println("Invalid token")
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "Invalid user token")).build()
        }

        val user = userService.findByEmail(email)

            ?: return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "User not found")).build()

        val tickets = ticketService.findByUserEmail(user.email)
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

        val userId = (jwt.getClaim<Any>("email") as? String)?.toString()
        if (userId == null) {
            println("Invalid token")
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "Invalid user token")).build()
        }
        val user1 = userService.findById(1)
        val user = userService.findByEmail(userId)

            ?: return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "User not found")).build()


        val ticket = Ticket(
            subject = request.subject,
            description = request.description,
            status = "open",
            priority = request.priority,
            createdAt = LocalDateTime.now(),
            systemUser = user

        )

        ticketService.saveTicket(ticket)
        return Response.status(Response.Status.CREATED).build()

    }


    @PUT
    @Path("/tickets/status/{id}")
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun updateTicketStatus(
        @PathParam("id") id: Long,
        status: StatusUpdateRequest
    ): Response {
        val ticket = ticketService.findById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity("ticket with id $id not found").build()

        ticket.status = status.status
        ticket.updatedAt = LocalDateTime.now()

        ticketService.saveTicket(ticket)

        return Response.ok(mapOf("message" to "Ticket updated successfully")).build()
    }




    @PUT
    @Path("/tickets/priority/{id}")
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun updateTicketPriority(
        @PathParam("id") id: Long,
        priority: PriorityUpdateRequest
    ): Response {
        val ticket = ticketService.findById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity("ticket with id $id not found").build()

        ticket.priority = priority.priority
        ticket.updatedAt = LocalDateTime.now()

        ticketService.saveTicket(ticket)

        return Response.ok(mapOf("message" to "Ticket updated successfully")).build()
    }






    @GET
    @Path("/tickets/view/{id}")
    @RolesAllowed("user", "admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun ticketDetails(@PathParam("id") id: Long): Response {
        val ticket = ticketService.findById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity("Ticket with ID $id not found").build()

        val reporter = ticket.systemUser

        val reporterDTO = SystemUserResponseDTO(
            id = reporter?.id,
            name = reporter?.name,
            email = reporter?.email,
            admin = reporter?.admin ?: false,
            issueType = reporter?.issueType,
            active = reporter?.active ?: false,
            institutionId = reporter?.institution?.id
        )

        val ticketResponseDTO = TicketResponseDTO(
            id = ticket.id,
            subject = ticket.subject,
            description = ticket.description,
            priority = ticket.priority,
            status = ticket.status,
            createdAt = ticket.createdAt,
            reporter = reporterDTO
        )

        return Response.ok(ticketResponseDTO).build()
    }




}