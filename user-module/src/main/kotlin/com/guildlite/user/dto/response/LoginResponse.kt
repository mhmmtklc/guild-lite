package com.guildlite.user.dto.response

data class LoginResponse(
    val token: String? = null,
    val user: UserResponse? = null,
    val success: Boolean = true,
    val message: String? = null
)
