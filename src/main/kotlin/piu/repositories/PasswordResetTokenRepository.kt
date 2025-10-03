package piu.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import org.piu.models.Ticket
import piu.models.PasswordResetToken


@ApplicationScoped
class PasswordResetTokenRepository: PanacheRepository<PasswordResetToken>{

    fun verifyResetToken(token: String): PasswordResetToken{
        return find("token" ,token).firstResult<PasswordResetToken>()
    }

}
