package com.guildlite.team.dto.response

data class JoinTeamResponse(

    val success: Boolean,
    val newToken: String? = null,
    val team: TeamResponse? = null,
    val memberCount: Int? = null,
    val message: String? = null

)
