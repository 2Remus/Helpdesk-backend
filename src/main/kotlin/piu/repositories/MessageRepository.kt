package org.piu.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import org.piu.models.Message

@ApplicationScoped
class MessageRepository: PanacheRepository<Message> {
    override fun findById(id: Long): Message?= find("id",id).firstResult<Message>()


    fun findBySender(sender: String): Message? {
        return find("sender",sender).firstResult<Message>()

    }


}