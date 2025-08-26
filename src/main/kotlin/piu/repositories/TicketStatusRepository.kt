package piu.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import org.piu.models.Ticket
import piu.models.TicketStatus

@ApplicationScoped
class TicketStatusRepository: PanacheRepository<TicketStatus> {
    override fun findById(id: Long): TicketStatus?= find("id",id).firstResult<TicketStatus>()

    fun findByName(name: String?): TicketStatus {
        return find("name", name).firstResult<TicketStatus>()
    }

}
