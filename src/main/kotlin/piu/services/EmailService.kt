package piu.services

import io.quarkus.mailer.Mail
import io.quarkus.mailer.Mailer
import io.quarkus.qute.Location
import io.quarkus.qute.Template
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response

@ApplicationScoped
class EmailService @Inject constructor(
    private val mailer: Mailer,
    @Inject
    @Location("activation.html")
    private val activationTemplate: Template,

    @Location("ticketAssignment.html")
    @Inject private val ticketAssignmentTemplate: Template,

    @Inject
    @Location("ticketStatusUpdate.html")
    private val ticketStatusUpdateTemplate: Template,

) {

    fun sendActivationEmail(to: String?, name: String, activationLink: String): Response {
        return try {
            val htmlBody = activationTemplate
                .data("name", name)
                .data("activationLink", activationLink)
                .render()

            val textBody = "Hello $name,\nActivate your account here: $activationLink"

            val mail = Mail.withText(to, "Activate Your Helpdesk Account", textBody)
                .setHtml(htmlBody).setFrom("boldxpressionvc@gmail.com")

            mailer.send(mail)
            Response.ok("Activation email sent to $to").build()
        } catch (e: Exception) {
            Response.serverError().entity("Could not send activation email").build()
        }
    }

    fun sendTicketAssignmentEmail(
        to: String?,
        assigneeName: String,
        ticketSubject: String?,
        priority: String,
        createdBy: String,
        ticketLink: String
    ): Response {
        return try {
            val htmlBody = ticketAssignmentTemplate
                .data("assigneeName", assigneeName)
                .data("ticketSubject", ticketSubject)
                .data("priority", priority)
                .data("createdBy", createdBy)
                .data("ticketLink", ticketLink)
                .render()

            val textBody = """
                Hello $assigneeName,
                
                A new ticket has been assigned to you.
                Subject: $ticketSubject
                Priority: $priority
                Created By: $createdBy
                
                View it here: $ticketLink
            """.trimIndent()

            val mail = Mail.withText(to, "New Ticket Assigned", textBody)
                .setHtml(htmlBody).setFrom("boldxpressionvc@gmail.com")

            mailer.send(mail)
            Response.ok("Ticket assignment email sent to $to").build()
        } catch (e: Exception) {
            Response.serverError().entity("Could not send ticket assignment email").build()
        }
    }


    fun sendTicketStatusUpdateEmail(
        to: String?,
        ticketOwner: String,
        ticketSubject: String?,
        priority: String,
        createdBy: String,
        ticketLink: String
    ): Response {
        return try {
            val htmlBody = ticketStatusUpdateTemplate
                .data("ticketOwner", ticketOwner)
                .data("ticketSubject", ticketSubject)
                .data("priority", priority)
                .data("createdBy", createdBy)
                .data("ticketLink", ticketLink)
                .render()

            val textBody = """
                Hello $ticketOwner,
                
                Your ticket status has been updated.
                Subject: $ticketSubject
                Priority: $priority
                Created By: $createdBy
                
                View it here: $ticketLink
            """.trimIndent()

            val mail = Mail.withText(to, "Ticket Status Update", textBody)
                .setHtml(htmlBody).setFrom("boldxpressionvc@gmail.com")

            mailer.send(mail)
            Response.ok("Ticket status update email sent to $to").build()
        } catch (e: Exception) {
            Response.serverError().entity("Could not send ticket update email").build()
        }
    }
}
