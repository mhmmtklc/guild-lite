package com.guildlite.user.dto.request

data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)
