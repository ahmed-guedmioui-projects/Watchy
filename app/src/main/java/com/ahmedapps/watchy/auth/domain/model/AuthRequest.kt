package com.ahmedapps.watchy.auth.domain.model

data class AuthRequest(
    val name: String = "",
    val email: String,
    val password: String = ""
)
