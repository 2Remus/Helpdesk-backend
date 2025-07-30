package piu.services

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import jakarta.enterprise.context.ApplicationScoped
import org.piu.models.SystemUser
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.util.Date

@ApplicationScoped
class JwtService {
    fun generateToken(user: SystemUser): String {
        val now = Date()
        val expiry = Date(now.time + 1000 * 60 * 60 * 6)

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

    fun loadPrivateKey(): RSAPrivateKey {
        val resource = Thread.currentThread().contextClassLoader.getResourceAsStream("privateKey.pem")
            ?: throw RuntimeException("privateKey.pem not found")

        val pem = resource.bufferedReader().use { it.readText() }
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s+".toRegex(), "")

        val decoded = Base64.getDecoder().decode(pem)
        val keySpec = PKCS8EncodedKeySpec(decoded)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }
}
