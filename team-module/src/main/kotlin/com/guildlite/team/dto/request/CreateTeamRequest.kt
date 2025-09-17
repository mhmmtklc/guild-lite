package com.guildlite.team.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateTeamRequest(

    @field:NotBlank(message = "Team name is required")
    @field:Size(min = 5, max = 50, message = "Team name must be between 5 and 50 characters")
    val name: String,

    @field:Size(min = 5, max = 150, message = "Description must not exceed 150 characters")
    val description: String,
)