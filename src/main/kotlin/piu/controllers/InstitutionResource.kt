package piu.controllers

import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.piu.models.Ticket
import org.piu.models.toDTO
import piu.models.Institution
import piu.models.InstitutionRequest
import piu.models.toDTO
import piu.services.InstitutionService
import project.cardtp.models.TicketRequest
import java.time.LocalDateTime

@Path("/api")
class InstitutionResource {
    @Inject
    lateinit var institutionService: InstitutionService

 /*   @GET
    @Path("/institutions")
    fun getAll(): List<Institution> = institutionService.findAll()
*/
    @GET
    @Path("/institutions")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional // Make sure the session is open while mapping
    fun findAll(): Response {
        val tickets = institutionService.findAll()
        val dtos = tickets.map { it.toDTO() }
        return Response.ok(dtos).build()
    }


    @GET
    @Path("/{id}")
    fun getById(@PathParam("id") id: Long): Response {
        val institution = institutionService.findById(id)
        return if (institution != null) Response.ok(institution).build()
        else Response.status(Response.Status.NOT_FOUND).build()
    }

    @POST
    fun create1(institution: Institution): Response {
        val created = institutionService.create(institution)
        return Response.status(Response.Status.CREATED).entity(created).build()
    }

    @POST
    @Path("/institutions/create")
    fun create(institution: Institution): Response {
        val created = institutionService.create(institution)
        return Response.status(Response.Status.CREATED).entity(created).build()
    }



    @PUT
    @Path("/{id}")
    fun update(@PathParam("id") id: Long, institution: Institution): Response {
        val updated = institutionService.update(id, institution)
        return if (updated != null) Response.ok(updated).build()
        else Response.status(Response.Status.NOT_FOUND).build()
    }

    @DELETE
    @Path("/{id}")
    fun delete(@PathParam("id") id: Long): Response {
        return if (institutionService.delete(id)) Response.noContent().build()
        else Response.status(Response.Status.NOT_FOUND).build()
    }


    @POST
    @Path("/institutions/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun create(request: InstitutionRequest): Response{
        val institution = Institution(
            name = request.name,
            address = request.address,
            email = request.email,
            phoneNumber = request.phoneNumber,
            createdAt = LocalDateTime.now()

        )

        institutionService.create(institution)
        return Response.status(Response.Status.CREATED).build()

    }

}