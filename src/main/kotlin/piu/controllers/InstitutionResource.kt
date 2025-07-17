package piu.controllers

import jakarta.inject.Inject
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.core.Response
import piu.models.Institution
import piu.services.InstitutionService

@Path("/api")
class InstitutionResource {
    @Inject
    lateinit var institutionService: InstitutionService

    @GET
    @Path("/institutions")
    fun getAll(): List<Institution> = institutionService.findAll()

    @GET
    @Path("/{id}")
    fun getById(@PathParam("id") id: Long): Response {
        val institution = institutionService.findById(id)
        return if (institution != null) Response.ok(institution).build()
        else Response.status(Response.Status.NOT_FOUND).build()
    }

    @POST
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

}