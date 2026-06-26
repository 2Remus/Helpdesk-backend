package piu.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import io.quarkus.panache.common.Parameters
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.NoResultException
import org.piu.models.SystemUser
import org.piu.models.Ticket
import piu.models.TicketAssignment


@ApplicationScoped
class TicketAssignmentRepository: PanacheRepository<TicketAssignment> {


    fun findByUser(user: SystemUser): TicketAssignment?{
        return find("assignedUser",user).firstResult<TicketAssignment>()

    }

    fun findCurrentTicketAssignment1(ticketId: Long): TicketAssignment =
        find("active = true and current = true and ticket.id = :ticketId",
            Parameters.with("ticketId", ticketId))
            .firstResult()
            ?: throw NoResultException("No active current assignment for ticket $ticketId")


    fun findCurrentTicketAssignment(ticketId: Long): TicketAssignment =
        find("active = true and current = true and ticket.id = :ticketId",
            Parameters.with("ticketId", ticketId))
            .firstResult()
            ?: throw NoResultException("No active current assignment for ticket $ticketId")

    fun hasCurrentTicketAssignment(ticketId: Long): Boolean {
        return find(
            "active = true and current = true and ticket.id = ?1",
            ticketId
        ).firstResultOptional<TicketAssignment>().isPresent
    }
}