package piu.models

data class UserRequest(
    var name: String,
    var email: String ,
    val password: String? = "",  // ✅ make it optional
    var admin: Boolean,
    var issueType: String,
    var institution: String,

)
