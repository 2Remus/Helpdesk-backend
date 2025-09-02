package piu.DTO

import jakarta.ws.rs.FormParam
import org.jboss.resteasy.annotations.providers.multipart.PartType
import java.io.InputStream

 class SignatureUploadRequest(

    @FormParam("userId")
    var userId: Long? = null,

    @FormParam("signature")
    @PartType("application/octet-stream")
    var signature: InputStream? = null
)
