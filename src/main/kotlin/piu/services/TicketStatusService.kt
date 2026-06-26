package piu.services

import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.piu.models.Ticket
import piu.models.TicketStatus
import piu.repositories.TicketStatusRepository

@ApplicationScoped
class TicketStatusService {
    @Inject
    lateinit var ticketStatusRepository: TicketStatusRepository

    fun findById(id: Long): TicketStatus?{
        return ticketStatusRepository.findById(id)

    }
    fun findAll(): List<TicketStatus>{
        return ticketStatusRepository.listAll(Sort.ascending("createdAt"))
    }

    fun findByName(name: String): TicketStatus{
        return ticketStatusRepository.findByName(name)
    }

    fun deleteTicketStatusPermanently(id: Long){
        ticketStatusRepository.deleteById(id)
    }

    fun saveTicketStatus(ticketStatus: TicketStatus){
        return ticketStatusRepository.persist(ticketStatus)
    }
}