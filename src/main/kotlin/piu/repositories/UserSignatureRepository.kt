package piu.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import io.quarkus.panache.common.Parameters
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.NoResultException
import org.piu.models.SystemUser
import piu.models.TicketAssignment
import piu.models.UserSignature

@ApplicationScoped
class UserSignatureRepository: PanacheRepository<UserSignature> {
    override fun findById(id: Long): UserSignature? = find("id",id).firstResult<UserSignature>()

    fun findByUserId(id: Long): UserSignature? = find("systemUser.id",id).firstResult<UserSignature>()

 //   fun findCurrentByUserId(id: Long): UserSignature? = find("active = true and systemUser.id",id).firstResult<UserSignature>()

    fun findCurrentByUserId(id: Long): UserSignature? =
        find("active = true and systemUser.id = ?1", id).firstResult()


    fun hasCurrentSignature(usid: Long): Boolean {
        return find(
            "active = true and systemUser.id = ?1",
            usid
        ).firstResultOptional<UserSignature>().isPresent
    }


    fun findCurrentSignature(usid: Long): UserSignature =
        find("active = true and systemUser.id = :usid",
            Parameters.with("systemUser_id", usid))
            .firstResult()
            ?: throw NoResultException("No active Signature for User $usid")
}