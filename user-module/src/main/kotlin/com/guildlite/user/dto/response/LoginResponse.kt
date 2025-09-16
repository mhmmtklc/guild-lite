package com.guildlite.user.dto.response

data class LoginResponse(
    val token: String,
    val user: UserResponse
)
