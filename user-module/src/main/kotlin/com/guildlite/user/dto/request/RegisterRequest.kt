package com.guildlite.user.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(

    @field:NotBlank(message = "Username is required.")
    @field:Size(min = 5, max = 30, message = "Username must be between 5 and 30 characters.")
    val username: String,


    @field:NotBlank(message = "Email is required.")
    @field:Email(message = "Email must be valid.")
    @field:Size(max = 100, message = "Email must be less than or equal to 100 characters.")
    val email: String,


    @field:NotBlank(message = "Password is required.")
    @field:Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters.")
    val password: String
)
