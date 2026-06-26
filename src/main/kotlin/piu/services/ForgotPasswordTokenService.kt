package piu.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.piu.models.Ticket
import piu.models.PasswordResetToken
import piu.repositories.PasswordResetTokenRepository

@ApplicationScoped
class ForgotPasswordTokenService {

    @Inject
    lateinit var passwordResetTokenRepository: PasswordResetTokenRepository

    fun saveResetToken(passwordResetToken: PasswordResetToken){
        return passwordResetTokenRepository.persist(passwordResetToken)
    }

    fun verifyResetToken(token: String): PasswordResetToken{
        return passwordResetTokenRepository.verifyResetToken(token);
    }


}