package piu.controllers

import io.quarkus.mailer.Mail
import io.quarkus.mailer.Mailer
import io.smallrye.common.annotation.Blocking
import io.smallrye.mutiny.Uni
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.angus.activation.MailcapParseException
import org.jboss.logging.Logger
import piu.DTO.EmailRequestDTO

@Path("/api")
class MailResource {
    @Inject
    lateinit var mailer: Mailer
    @Inject
    lateinit var log: Logger

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/sendmail")
    fun sendEmail() {
        mailer.send(
            Mail.withText(
                "nhenry@gov.vc",           // To
                "Quarkus email test",            // Subject
                "A simple email sent from a Quarkus application.  Regards Neilon" // Body
            )
        )
    }



    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun sendEmail(request: EmailRequestDTO): Uni<Response> {
        return Uni.createFrom().item {
            try {
                mailer.send(
                    Mail.withText(
                        request.to,
                        request.subject,
                        request.body
                    )
                )
                Response.ok(mapOf("message" to "Email sent to ${request.to}")).build()
            } catch (e: MailcapParseException) {
                log.error("Failed to send email", e)
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "Failed to send email: ${e.message}")).build()
            }
        }
    }


    @GET
    @Blocking
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/sendemailtest")

    fun sendEmailTest(): Uni<Response> {
        return Uni.createFrom().item {
            try {
                mailer.send(
                    Mail.withText(
                        "nhenry@gov.vc",
                        "Test email sender",
                        "Just a test, Regards, Neilon"
                    )
                )
                Response.ok(mapOf("message" to "Email sent to nhenry@gov.vc")).build()
            } catch (e: MailcapParseException) {
                log.error("Failed to send email", e)
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "Failed to send email: ${e.message}")).build()
            }
        }
    }

}