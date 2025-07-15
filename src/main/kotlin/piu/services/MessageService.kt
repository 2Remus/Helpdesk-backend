package org.piu.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.piu.models.Message
import org.piu.repositories.MessageRepository

@ApplicationScoped
class MessageService {

    @Inject
    lateinit var messageRepository: MessageRepository
    fun findAll(): List<Message>{
        return messageRepository.listAll()
    }

    fun findByEmail(sender: String): Message?{
        return messageRepository.findBySender(sender)
    }

    fun findByTicketId(ticketId: Long): List<Message> {
        return messageRepository.list("ticket.id", ticketId)
    }


    fun save(message: Message) {
        messageRepository.persist(message)
    }

}