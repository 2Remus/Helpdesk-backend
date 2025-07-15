package org.piu.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import org.piu.models.Ticket

@ApplicationScoped
class TicketRepository : PanacheRepository<Ticket>{
    override fun findById(id: Long): Ticket?= find("id",id).firstResult<Ticket>()

    fun findBySubject(subject: String): Ticket?{
        return find("subject",subject).firstResult<Ticket>()

    }
}