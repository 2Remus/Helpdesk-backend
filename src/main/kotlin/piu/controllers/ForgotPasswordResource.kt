package piu.controllers

import io.quarkus.elytron.security.common.BcryptUtil
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import org.piu.services.UserService
import piu.DTO.ForgotPasswordRequest
import piu.DTO.PasswordResetRequest
import piu.models.PasswordResetToken
import piu.services.EmailService
import piu.services.ForgotPasswordTokenService
import java.time.LocalDateTime
import java.util.UUID


@Path("/api")
class ForgotPasswordResource {



    @Inject
    lateinit var userService: UserService
    @Inject
    lateinit var passwordTokenService: ForgotPasswordTokenService

    @Inject
    lateinit var emailService: EmailService

    @POST
    @Path("/forgot-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
  //  @RolesAllowed("admin","user")
    @Transactional
    fun forgotPassword(request: ForgotPasswordRequest): Response{

        val user = userService.findByEmail(request.email)
        if(user == null){
            return Response.status(400).entity(mapOf("error" to "User not found")).build();
        }

        val token = UUID.randomUUID().toString();
        val passwordResetToken = PasswordResetToken(
                token = token,
                systemUser = user,
                expiryDate = LocalDateTime.now().plusHours(1)
            )
            passwordTokenService.saveResetToken(passwordResetToken)
            val resetLink = "http://138.68.58.185/help-desk/reset-password?token=$token"
            try {
                emailService.sendResetPasswordEmail(
                    to = user.email,
                    name = user.name ,
                    resetLink = resetLink
                )

            } catch (e: Exception) {
                // Consider rolling back the user or allowing resend later
                println("Failed to send reset mail to ${user.email}"+ e)
                return Response.serverError().entity("Could not send reset email. Please try again.").build()
            }



        return Response.ok("Link sent to email.").build()

    }


    @POST
    @Path("/reset-password/{token}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //  @RolesAllowed("admin","user")
     @Transactional
    fun resetPassword(@PathParam("token") token: String,request: PasswordResetRequest): Response{

        val resetToken = passwordTokenService.verifyResetToken(token)
        println("Reset Token:  " + resetToken.systemUser?.email)
       if(resetToken == null || resetToken.expiryDate?.isBefore(LocalDateTime.now()) == true){
           println("Valid token")
           return Response.status(400).entity(mapOf("error" to "Invalid request")).build()

       }

        val user =  resetToken.systemUser;//userService.findByEmail(request.email)
        val newHash = BcryptUtil.bcryptHash(request.password);
        println("New Hash: "+newHash)
        user?.hashedPassword =  newHash;
        user?.updatedAt = LocalDateTime.now();
        userService.updateUser(user);
        println("hashed pass "+newHash)
        return Response.ok("Password reset successfully").build()
    }

}