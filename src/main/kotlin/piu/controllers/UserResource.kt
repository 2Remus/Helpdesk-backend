package org.piu.controllers

import io.quarkus.elytron.security.common.BcryptUtil
import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.FormParam
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.piu.models.SystemUser
import org.piu.services.UserService
import piu.models.SystemUserDTO
import piu.models.SystemUserResponseDTO
import piu.models.UserRequest
import piu.models.UserRoleRequest
import piu.models.UserStatusRequest
import piu.models.toDTO
import java.time.LocalDateTime
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm
import org.jboss.resteasy.annotations.providers.multipart.PartType
import piu.models.UserSignature
import piu.services.UserSignatureService
import java.io.ByteArrayInputStream
import java.io.InputStream


@Path("/api")
class UserResource {
    @Inject
    lateinit var userService: UserService



@Inject
lateinit var userSignatureService: UserSignatureService
    @GET
    @Path("/users")
   // @RolesAllowed("admin" )
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    fun findAll(): Response{
            val users = userService.findAll()
            val userdtos = users.map { it.toDTO() }
        return Response.ok(userdtos).build()
    }


    @GET
    @Path("/available-users")
    @RolesAllowed("admin" )
    @Produces(MediaType.APPLICATION_JSON)
    fun findAdminUsers(): Response{
        val users = userService.findAvailableUsers()
        val userdtos = users.map { it.toDTO() }
        return Response.ok(userdtos).build()
    }

    @POST
  //  @RolesAllowed("admin" )
    @Path("/users/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun createUser(
        @PathParam("ticketId") ticketId: Long,
        dto: SystemUserDTO
    ): Response {

        val newBcrypt = BcryptUtil.bcryptHash(dto.password)

        val user = SystemUser()
        user.email = dto.email
        user.name = dto.name
        user.admin = dto.admin
        user.hashedPassword  = newBcrypt
        user.createdAt = LocalDateTime.now()
        user.issueType = dto.issueType

        userService.save(user)
        // Map to DTO before returning
       val userResponseDTO = SystemUserResponseDTO(
           id = user.id!!,
           name = user.name,
           email = user.email,
           issueType = user.issueType,
           admin = user.admin,
           active = user.active,
           institutionId = user.institution?.id

       )
       return Response.status(Response.Status.CREATED).entity(userResponseDTO).build()
    }


    @DELETE
    @Path("/users/{userId}")
    @RolesAllowed("admin" )
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun deleteUser(@PathParam("userId") userId: Long): Response {
        val user = userService.findById(userId)
            ?: return Response.status(Response.Status.NOT_FOUND).entity("User not found").build()

        userService.deleteUser(userId)

        return Response.status(Response.Status.NO_CONTENT).build()
    }




    @GET
    @Path("/users/{usId}")
    @RolesAllowed("admin" )
    @Produces(MediaType.APPLICATION_JSON)
    fun getById(@PathParam("usId") id: Long): Response {
        val user = userService.findById(id)
        return if (user != null) {
            val dto = user.toDTO() // cleaner with extension function
            Response.ok(dto).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @PUT
    @Path("/users/edit/{id}")
    @RolesAllowed("admin" )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun updateUser(
        @PathParam("id") id: Long,
        request: UserRequest
    ): Response {
        val user = userService.findById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity("User with id $id not found").build()

        val type = request.issueType ?:"";

        user.name = request.name
        user.email = request.email
        user.admin = request.admin
        user.issueType = type
        user.updatedAt = LocalDateTime.now()

        userService.updateUser(user)
        return Response.status(Response.Status.OK).build()

    }



    @PUT
    @Path("/users/edit/role/{id}")
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun updateUserRole(
        @PathParam("id") id: Long,
        request: UserRoleRequest
    ): Response {
        val user = userService.findById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity("User with id $id not found").build()

        user.admin = request.admin
        user.issueType = request.issueType
        user.updatedAt = LocalDateTime.now()

        userService.save(user)

        return Response.ok(mapOf("message" to "User updated successfully")).build()
    }


    @PUT
    @Path("/users/edit/activeStatus/{id}")
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun updateUserActiveStatus(
        @PathParam("id") id: Long,
        request: UserStatusRequest
    ): Response {
        val user = userService.findById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity("User with id $id not found").build()

        user.active = request.active
        user.updatedAt = LocalDateTime.now()

        userService.save(user)

        return Response.ok(mapOf("message" to "User updated successfully")).build()
    }


/*
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    fun uploadImage(
        @FormParam("userId") userId: Long,
        @FormParam("file") file: FileUpload
    ): Response {
        val user = userService.findById(userId)
            ?: return Response.status(Response.Status.NOT_FOUND).entity("User not found").build()

        val bytes = file.uploadedFile().toFile().readBytes()
        user.image = bytes
        user.updatedAt = LocalDateTime.now()

        userService.save(user)

        return Response.ok("Image uploaded successfully").build()
    }*/


/*

    @PUT
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @PermitAll
    @Transactional
    fun uploadImage1(form: ImageUploadForm): Response {
        println("Uploading")
        val user = userService.findById(form.userId!!)
            ?: return Response.status(Response.Status.NOT_FOUND).entity("User not found").build()

       val bytes = form.file.readAllBytes()
        user.image = bytes
        user.updatedAt = LocalDateTime.now()
        userService.save(user)

        return Response.ok("Image uploaded successfully").build()
    }*/


    @POST
    @Path("/users/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @RolesAllowed("admin")
    fun uploadImage(@MultipartForm form: ImageUploadForm): Response {
        println("Uploading ${form.userId}")
        val user = userService.findById(form.userId!!)
            ?: return Response.status(Response.Status.NOT_FOUND).entity("User not found").build()

       // val bytes = form.file.readAllBytes()
        val bytes = form.file.readBytes()
        user.image = bytes
        user.updatedAt = LocalDateTime.now()
        userService.save(user)

        return Response.ok("Image uploaded successfully").build()
    }




    @GET
    @Path("/users/{id}/image")
    fun getUserImage(@PathParam("id") id: Long): Response {
        val user = userService.findById(id)
            ?: return Response.status(Response.Status.NOT_FOUND).entity("User not found").build()

        if (user.image == null) {
            return Response.status(Response.Status.NO_CONTENT).build()
        }

        return Response.ok(ByteArrayInputStream(user.image))
            .type("image/png") // ⚠️ assumes PNG; change if storing JPG etc.
            .build()
    }



    class ImageUploadForm {
        @FormParam("userId")
        var userId: Long? = null

        @FormParam("file")
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        lateinit var file: InputStream
    }




    @POST
    @Path("/users/signature")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @RolesAllowed("admin")
    fun uploadSignature(@MultipartForm form: SignatureUploadRequest): Response {
        println("Received userId=${form.userId}, signature=${form.signature != null}")
        if (form.userId == null) {
            println("User is null")
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Missing userId").build()
        }


        val user = userService.findById(form.userId!!)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity("User not found").build()

        val hasSignature =  userSignatureService.hasCurrentSignature(form.userId!!)
        if(hasSignature){
            val signature = userSignatureService.findByUserId(form.userId!!)
            signature?.active = false
            signature?.updatedAt = LocalDateTime.now()
            userSignatureService.save(signature)


        }
        val bytes = form.signature?.readBytes()
            ?: return Response.status(Response.Status.BAD_REQUEST)
                .entity("Missing signature file").build()


        val userSignature = UserSignature(
            systemUser = user,
            createdAt = LocalDateTime.now(),
            active = true,
            signature = bytes
        )
        userSignatureService.save(userSignature)

        return Response.ok("Signature uploaded successfully").build()
    }


    class SignatureUploadRequest {
        @FormParam("userId")
        var userId: Long? = null

        @FormParam("signature")
        @PartType("application/octet-stream")
        var signature: InputStream? = null
    }


    @GET
    @Path("/users/{id}/signature")
    @Produces("image/png")
    fun getSignature(@PathParam("id") id: Long): Response {
        val user = userService.findById(id) ?: throw NotFoundException("User not found")
        val usersig = userSignatureService.findCurrentByUserId(id)
        return if (usersig?.signature != null)
            Response.ok(usersig.signature).build()
        else
            Response.status(Response.Status.NOT_FOUND).build()
    }



}