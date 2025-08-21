package piu.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.piu.models.Ticket
import piu.models.TicketAssignment
import piu.repositories.TicketAssignmentRepository

@ApplicationScoped
class TicketAssignmentService {
    @Inject
    lateinit var ticketAssignmentRepository: TicketAssignmentRepository

    fun saveTicketAssignment(ticketAssignment: TicketAssignment){
        return ticketAssignmentRepository.persist(ticketAssignment)
    }
    fun findCurrent(id: Long): TicketAssignment{
        return ticketAssignmentRepository.findCurrentTicketAssignment(id);
    }

    fun hasCurrent(id: Long): Boolean{
        return ticketAssignmentRepository.hasCurrentTicketAssignment(id);
    }
}