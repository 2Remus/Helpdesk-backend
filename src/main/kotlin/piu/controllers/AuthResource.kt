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
import org.piu.repositories.UserRepository
import piu.models.LoginRequest
import piu.models.LoginResponse
import piu.models.SystemUserDTO
import java.security.MessageDigest
import java.time.Duration
import io.smallrye.jwt.build.Jwt
import org.piu.services.UserService
import java.nio.file.Files
import java.nio.file.Paths
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

@Path("/api")
class AuthResource {
    @Inject
    lateinit var userService: UserService

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun login(req: LoginRequest): Response {
        println("Login request for: ${req.email}")
        val user = userService.findByEmail(req.email)
            ?: return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build()

        //  val hashed = BcryptUtil.bcryptHash(req.password)
        println("User"+user.email)
        return when {
            // Already using bcrypt

            user.hashedPassword?.startsWith("$2a$") == true || user.hashedPassword ?.startsWith("$2b$") == true -> {
                if (!BcryptUtil.matches(req.password, user.hashedPassword)) {
                    println("Req: "+req.email)
                    println("Bcrypt password mismatch"+" "+user.hashedPassword)
                    Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build()
                } else {
                    println("Bcrypt match")
                    createJwtResponse(user)
                }
            }

            // Fallback to SHA-256 check
            sha256(req.password) == user.hashedPassword -> {
                println("SHA256 match. Migrating to bcrypt...")
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



}