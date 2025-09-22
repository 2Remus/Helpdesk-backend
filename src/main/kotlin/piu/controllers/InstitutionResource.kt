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
import piu.models.Institution
import piu.models.InstitutionRequest
import piu.models.toDTO
import piu.services.InstitutionService
import java.time.LocalDateTime

@Path("/api")
class InstitutionResource {
    @Inject
    lateinit var institutionService: InstitutionService

 /*   @GET
    @Path("/institutions")
    fun getAll(): List<Institution> = institutionService.findAll()
*/
    /*get institution list. functional 18-July-2025*/
    @GET
    @Path("/institutions")
    @RolesAllowed("admin" )
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional // Make sure the session is open while mapping
    fun findAll(): Response {
        val institutions = institutionService.findAll()
        val dtos = institutions.map { it.toDTO() }
        return Response.ok(dtos).build()
    }


    @GET
    @RolesAllowed("admin" )
    @Path("/institutions/{id}")
    fun getById(@PathParam("id") id: Long): Response {
        val institution = institutionService.findById(id)
        return if (institution != null) Response.ok(institution).build()
        else Response.status(Response.Status.NOT_FOUND).build()
    }


    @DELETE
    @Path("/{id}")
    fun delete(@PathParam("id") id: Long): Response {
        return if (institutionService.delete(id)) Response.noContent().build()
        else Response.status(Response.Status.NOT_FOUND).build()
    }

    /*create new institution. functional 18-July-2025*/
    @POST
    @Path("/institutions/create")
    @RolesAllowed("admin" )
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

    @PUT
    @Path("/institutions/edit/{id}")
    @RolesAllowed("admin" )

    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun updateInstitution(
        @PathParam("id") id: Long,
        request: InstitutionRequest
    ): Response {
        val institution = institutionService.findById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity("Institution with id $id not found").build()

        institution.name = request.name
        institution.address = request.address
        institution.email = request.email
        institution.phoneNumber = request.phoneNumber
        institution.updatedAt = LocalDateTime.now()

        institutionService.updateInstitution(institution)
        return Response.status(Response.Status.OK).build()

    }

    @DELETE
    @Path("/institutions/{instId}")
    @RolesAllowed("admin" )

    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun deleteInstitution(@PathParam("instId") instId: Long): Response {
        val user = institutionService.findById(instId)
            ?: return Response.status(Response.Status.NOT_FOUND).entity("Institution not found").build()

        institutionService.delete(instId)

        return Response.status(Response.Status.NO_CONTENT).build()
    }



}