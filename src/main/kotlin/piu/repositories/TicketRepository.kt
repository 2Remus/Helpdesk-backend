package org.piu.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.validation.constraints.Email
import org.piu.models.SystemUser
import org.piu.models.Ticket
import piu.models.Institution

@ApplicationScoped
class TicketRepository : PanacheRepository<Ticket>{
    override fun findById(id: Long): Ticket?= find("id",id).firstResult<Ticket>()

    fun findBySubject(subject: String): Ticket?{
        return find("subject",subject).firstResult<Ticket>()

    }

    fun findByUserEmail(email: String?): List<Ticket> {
        return find("systemUser.email", email).list()
    }



    fun findByUserIssueType(issueType: String?): List<Ticket> {
        return find("issueType", issueType).list()
    }


    fun findTicketsAssignedToUser(user: String?): List<Ticket> {
        return find("assignedTo", user).list()
    }


}