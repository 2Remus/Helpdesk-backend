package piu.controllers

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
import org.piu.models.Message
import org.piu.models.toDTO
import org.piu.services.MessageService
import org.piu.services.TicketService
import org.piu.services.UserService
import piu.models.MessageDTO
import piu.models.MessageResponseDTO
import java.time.LocalDateTime

@Path("/api")
class MessageResource {
    @Inject
    lateinit var messageService: MessageService

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var ticketService: TicketService
    @GET
    @Path("/messages")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional // Make sure the session is open while mapping

    fun findAll(): Response {
        return Response.ok(messageService.findAll()).build()
    }



/*
    @GET
    @Path("/tickets/{ticketId}/messages")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun findMessagesByTicketId1(@PathParam("ticketId") ticketId: Long): Response {
        val messages = messageService.findByTicketId(ticketId)
        val user = userService.findById(1) // to be replaced with authenticated user when ready

        val dtoList = messages.map {
            MessageResponseDTO(
                id = it.id!!,
                content = it.content,
                createdAt = it.createdAt,
                ticketId = it.ticket?.id!!,
                userId = user?.id
            )
        }
        return Response.ok(dtoList).build()
    }
    */

    @GET
    @Path("/tickets/{ticketId}/messages")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun findMessagesByTicketId(@PathParam("ticketId") ticketId: Long): Response {
        val messages = messageService.findByTicketId(ticketId)

      /*  val dtoList = messages.map { message ->
            MessageResponseDTO(
                id = message.id!!,
                content = message.content,
                createdAt = message.createdAt,
                ticketId = message.ticket?.id!!,
                ticketSubject = message.ticket?.subject, // Load within session
                senderEmail = message.sender // Load within session
            )}*/
        val dtoList = messages.map { it.toDTO() }
        return Response.ok(dtoList).build()
    }


    @POST
    @Path("/tickets/{ticketId}/message")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun sendMessageByTicketId(
        @PathParam("ticketId") ticketId: Long,
        dto: MessageDTO
    ): Response {
        val ticket = ticketService.findById(ticketId)
            ?: return Response.status(Response.Status.NOT_FOUND).entity("Ticket not found").build()

        val user = userService.findById(1) // Replace with authenticated user when ready

        val message = Message()
        message.content = dto.content
        message.ticket = ticket
        message.sender = user?.email
        message.createdAt = LocalDateTime.now()

        messageService.save(message)
        // Map to DTO before returning
        val responseDTO = MessageResponseDTO(
            id = message.id!!,
            content = message.content,
            createdAt = message.createdAt,
            ticketId = message.ticket?.id!!,
            ticketSubject = message.ticket?.subject,
            sender = message.sender
        )

        return Response.status(Response.Status.CREATED).entity(responseDTO).build()
    }

}