package org.piu.services

import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.piu.models.Ticket
import org.piu.repositories.TicketRepository


@ApplicationScoped
@Transactional
class TicketService {
    @Inject
    lateinit var ticketRepository: TicketRepository


    fun findById(id: Long): Ticket?{
        return ticketRepository.findById(id)

    }
    fun findAll(): List<Ticket>{
        return ticketRepository.listAll(Sort.ascending("createdAt"))
    }

    fun findByEmail(subject: String): Ticket?{
        return ticketRepository.findBySubject(subject)
    }


    fun findByUserEmail(email: String?): List<Ticket>{
        return ticketRepository.findByUserEmail(email)
    }





    fun saveTicket(ticket: Ticket){
        return ticketRepository.persist(ticket)
    }
}