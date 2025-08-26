package piu.controllers

import io.quarkus.elytron.security.common.BcryptUtil
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.piu.models.SystemUser
import piu.models.LoginRequest
import piu.models.LoginResponse
import piu.models.SystemUserDTO
import java.security.MessageDigest
import org.piu.services.UserService
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.util.Date
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.crypto.RSASSASigner
import io.quarkus.mailer.Mail
import io.quarkus.mailer.Mailer
import jakarta.annotation.security.PermitAll
import jakarta.transaction.Transactional
import jakarta.ws.rs.GET
import jakarta.ws.rs.PathParam
import piu.models.RegisterRequest
import piu.services.EmailService
import java.util.UUID

@Path("/api")
class AuthResource {
    @Inject
    lateinit var userService: UserService


    @Inject
    lateinit var emailService: EmailService


    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun login(req: LoginRequest): Response {
        val user = userService.findByEmail(req.email)
            ?: return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build()
       if(!user.active){
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build()
        }
        return when {
            // Already using bcrypt
            user.hashedPassword?.startsWith("$2a$") == true || user.hashedPassword ?.startsWith("$2b$") == true -> {
                if (!BcryptUtil.matches(req.password, user.hashedPassword)) {
                    Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build()
                } else {
                    createJwtResponse(user)
                }
            }

            // Fallback to SHA-256 check
            sha256(req.password) == user.hashedPassword -> {
                // Re-hash with bcrypt and update the user
                val newBcrypt = BcryptUtil.bcryptHash(req.password)
                user.hashedPassword = newBcrypt
               userService.updateUser(user) // Save to DB
                createJwtResponse(user)
            }

            else -> {
                println("Password did not match any method")
                Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build()
            }
        }
    }


    fun createJwtResponse(user: SystemUser): Response {
        val token = generateJwtManually(user)
        val userDTO = SystemUserDTO(
            id = user.id,
            email = user.email,
            admin = user.admin,
            issueType = user.issueType,
            name = user.name,
            password = "",
            active = user.active,
            institutionId = 0
        )
        return Response.ok(LoginResponse(token , userDTO)).build()
    }


    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /*loads the privateKey from the resources directory, ensures that it is in proper format*/
    fun loadPrivateKey(): RSAPrivateKey {
        val resource = Thread.currentThread().contextClassLoader.getResourceAsStream("privateKey.pem")
            ?: throw RuntimeException("privateKey.pem not found in resources")
        val pem = resource.bufferedReader().use { it.readText() }
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s+".toRegex(), "")
        val decoded = Base64.getDecoder().decode(pem)
        val keySpec = PKCS8EncodedKeySpec(decoded)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }

    /*generates the token needed for login*/
    fun generateJwtManually(user: SystemUser): String {
        val now = Date()
        val expiry = Date(now.time + 1000 * 60 * 60 * 6) // 6 hours

        val claims = JWTClaimsSet.Builder()
            .issuer("cardtp")
            .subject(user.email)
            .issueTime(now)
            .expirationTime(expiry)
            .claim("id", user.id)
            .claim("email", user.email)
            .claim("admin", user.admin)
            .claim("groups", if (user.admin) listOf("admin") else listOf("user"))
            .build()

        val signer = RSASSASigner(loadPrivateKey())

        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build(),
            claims
        )

        signedJWT.sign(signer)

        return signedJWT.serialize()
    }
    @Inject
    lateinit var mailer: Mailer

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    fun register(req: RegisterRequest): Response {

        val userExist = userService.findByEmail(req.email)
        if (userExist != null) return Response.status(Response.Status.CONFLICT).entity("Email already exists").build()

        val token = UUID.randomUUID().toString()
        val hashedPassword = BcryptUtil.bcryptHash(req.password)
        val user = SystemUser(
            name = req.name,
            email = req.email,
            hashedPassword = hashedPassword,
            activationToken = token,
            active = false
        )
        userService.save(user)
        val activationLink = "http://192.168.1.112/help-desk/activate?token=$token"
        try {
            mailer.send(
                Mail.withText(
                    user.email,
                    "Activate Your Helpdesk Account",
                    "Click the link to activate your account: $activationLink"
                ).setFrom("boldxpressionvc@gmail.com")
            )
        } catch (e: Exception) {
            // Consider rolling back the user or allowing resend later
           println("Failed to send activation mail to ${user.email}"+ e)
            return Response.serverError().entity("Could not send activation email. Please try again.").build()
        }

        return Response.ok("Registration successful, check your email to activate your account.").build()
    }




    @POST
    @Path("/activate/{token}")
    @PermitAll
    @Transactional
    fun activateUser(@PathParam("token") token: String): Response {
        println("activation started: $token")

        val user = userService.findByActivationToken(token)
        println("User to activate: ${user?.email}")
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("Invalid token")
                .build()
        }

        user.active = true
        user.activationToken = null
        userService.updateUser(user)

        return Response.ok("Account activated. You may now log in.").build()
    }



}