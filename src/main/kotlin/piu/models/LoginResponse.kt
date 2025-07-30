package piu.models

import piu.controllers.AuthResource

data class LoginResponse(

    val token: String,
    val user: SystemUserDTO
)
