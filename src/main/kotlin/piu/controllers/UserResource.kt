package org.piu.controllers

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.piu.services.UserService

@Path("/api")
class UserResource {
    @Inject
    lateinit var userService: UserService

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    fun findAll(): Response{
        return Response.ok(userService.findAll()).build()
    }

}