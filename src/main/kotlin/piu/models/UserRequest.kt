package piu.models

data class UserRequest(
    var name: String,
    var email: String ,
    var password: String,
    var admin: Boolean,
    var issueType: String,
    var institution: String,

)
