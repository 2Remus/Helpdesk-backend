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
import piu.DTO.IssueTypeUpdateRequest
import piu.DTO.TicketAssignmentRequest
import piu.models.PriorityUpdateRequest
import piu.models.StatusUpdateRequest
import piu.models.TicketAssignment
import piu.models.TicketResponseDTO
import piu.models.toDTO
import piu.services.EmailService
import piu.services.TicketAssignmentService
import project.cardtp.models.TicketRequest
import java.time.LocalDateTime

@Path("/api")
class TicketResource {
    @Inject
    lateinit var ticketService: TicketService

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var ticketAssignmentService: TicketAssignmentService

    @Inject
    lateinit var emailService: EmailService


    @Inject
    lateinit var jwt: JsonWebToken

    @GET
    @Path("/tickets/institution")
    @RolesAllowed("admin" )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional // Make sure the session is open while mapping
    fun findAllByInstitution(): Response {
        val tickets = ticketService.findAll()
        val dtos = tickets.map { it.toDTO() }
        return Response.ok(dtos).build()
    }


    @GET
    @Path("/tickets")
    @RolesAllowed("admin","view tickets")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional // Make sure the session is open while mapping
    fun findAll(): Response {
        val tickets = ticketService.findAll()
        val dtos = tickets.map { it.toDTO() }
        return Response.ok(dtos).build()
    }

    @GET
    @Path("/tickets-by-issues")
   // @RolesAllowed("view categorized tickets" )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional // Make sure the session is open while mapping
    fun findByIssueType(): Response {

        val email = (jwt.getClaim<Any>("email") as? String)?.toString()
        if (email == null) {
            println("Invalid token")
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "Invalid user token")).build()
        }

        val user = userService.findByEmail(email)

            ?: return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "User not found")).build()

        val tickets = ticketService.findByUserIssueType(user.issueType)
        val dtos = tickets.map { it.toDTO() }
        return Response.ok(dtos).build()
    }

    @GET
    @Path("/tickets/my-assigned")
    @RolesAllowed("admin" )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional // Make sure the session is open while mapping
    fun assignedTickets(): Response {

        val email = (jwt.getClaim<Any>("email") as? String)?.toString()
        if (email == null) {
            println("Invalid token")
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "Invalid user token")).build()
        }

        val user = userService.findByEmail(email)

            ?: return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "User not found")).build()

        val tickets = ticketService.findTicketsAssignedToUser(user.name)
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
    @RolesAllowed("admin","user","create ticket")
    @Transactional
    fun saveTicket(request: TicketRequest): Response{

        val userId = (jwt.getClaim<Any>("email") as? String)?.toString()
        if (userId == null) {
            println("Invalid token")
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "Invalid user token")).build()
        }
        val user = userService.findByEmail(userId)

            ?: return Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to "User not found")).build()


        val ticket = Ticket(
            subject = request.subject,
            description = request.description,
            status = "Open",
            priority = "low",//request.priority,
            createdAt = LocalDateTime.now(),
            systemUser = user,
            issueType = request.type

        )

        ticketService.saveTicket(ticket)
        return Response.status(Response.Status.CREATED).build()

    }


    @PUT
    @Path("/tickets/status/{id}")
    @RolesAllowed("admin","update ticket status")
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

        val ticketLink = "http://10.181.1.64/tickets/view/${ticket.id}"

        val ticketOwner = ticket.systemUser;
        if (ticketOwner?.email.isNullOrBlank()) {
            println("No email found for user: ${ticketOwner?.email}")
        } else {
            println("Sending email to ${ticketOwner.email}")
        }
        // make sure email is not null
        ticketOwner?.email?.let { email -> emailService.sendTicketStatusUpdateEmail(
                email,
                ticket.assignedTo,
                ticket.subject,
                ticket.priority,
                ticket.systemUser?.name ?: "System",
                ticketLink
            )
        }

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




    @PUT
    @Path("/tickets/assign/{id}")
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun updateTicketAssignment(
        @PathParam("id") id: Long,
        assignTo: TicketAssignmentRequest
    ): Response {
        val ticket = ticketService.findById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity("ticket with id $id not found").build()

        val assignee = ticketAssignmentService.hasCurrent(id)
        if (assignee) {
            val existingAssignment = ticketAssignmentService.findCurrent(id)
            existingAssignment.updatedAt = LocalDateTime.now()
            existingAssignment.current = false
            ticketAssignmentService.saveTicketAssignment(existingAssignment)
        }

        if (assignTo.assignment != "Unassigned") {
            val user = userService.findByName(assignTo.assignment)
                ?: return Response.status(Response.Status.BAD_REQUEST)
                    .entity("User '${assignTo.assignment}' not found").build()

            val ticketAssignment = TicketAssignment(
                createdAt = LocalDateTime.now(),
                current = true,
                active = true,
                ticket = ticket,
                assignedUser = user
            )
            ticketAssignmentService.saveTicketAssignment(ticketAssignment)

            // update ticket before sending email
            ticket.assignedTo = assignTo.assignment
            ticket.updatedAt = LocalDateTime.now()
            ticketService.saveTicket(ticket)

            val ticketLink = "http://10.181.1.64/tickets/view/${ticket.id}"

            if (user.email.isNullOrBlank()) {
                println("No email found for assigned user: ${assignTo.assignment}")
            } else {
                println("Sending email to ${user.email}")
            }
            // make sure email is not null
            user.email?.let { email ->
                emailService.sendTicketAssignmentEmail(
                    email,
                    ticket.assignedTo,
                    ticket.subject,
                    ticket.priority,
                    ticket.systemUser?.name ?: "System",
                    ticketLink
                )
            }

        } else {
            // unassign case
            ticket.assignedTo = "Unassigned"
            ticket.updatedAt = LocalDateTime.now()
            ticketService.saveTicket(ticket)
        }

        return Response.ok(mapOf("message" to "Ticket updated successfully")).build()
    }






    @GET
    @Path("/tickets/view/{id}")
    @RolesAllowed("view ticket", "admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun ticketDetails(@PathParam("id") id: Long): Response {
        val ticket = ticketService.findById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity("Ticket with ID $id not found").build()

        val reporter = ticket.systemUser
       val reporterDTO = ticket.systemUser?.toDTO()

        val ticketResponseDTO = TicketResponseDTO(
            id = ticket.id,
            subject = ticket.subject,
            description = ticket.description,
            priority = ticket.priority,
            status = ticket.status,
            createdAt = ticket.createdAt,
            reporter = reporterDTO,
            assignedTo = ticket.assignedTo,
            issueType = ticket.issueType,
        )

        return Response.ok(ticketResponseDTO).build()
    }




    @PUT
    @Path("/tickets/issue-type/{id}")
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun updateTicketIssueType(
        @PathParam("id") id: Long,
        request: IssueTypeUpdateRequest
    ): Response {
        val ticket = ticketService.findById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity("ticket with id $id not found").build()

        ticket.issueType = request.issueType
        ticket.updatedAt = LocalDateTime.now()

        ticketService.saveTicket(ticket)

        return Response.ok(mapOf("message" to "Ticket updated successfully")).build()
    }



}