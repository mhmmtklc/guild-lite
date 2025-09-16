package com.guildlite.user.dto.response

import com.guildlite.user.entity.UserEntity
import java.time.LocalDateTime

data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val status: UserEntity.UserStatus? = null,
    val createdAt: LocalDateTime? = null,
    val lastLoginAt: LocalDateTime? = null
)
