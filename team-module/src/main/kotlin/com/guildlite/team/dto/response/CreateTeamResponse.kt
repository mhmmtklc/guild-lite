package com.guildlite.team.dto.response

data class CreateTeamResponse(

    val success: Boolean,
    val newToken: String? = null,
    val team: TeamResponse? = null,
    val message: String? = null

)
