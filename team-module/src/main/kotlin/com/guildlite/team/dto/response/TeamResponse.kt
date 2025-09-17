package com.guildlite.team.dto.response

import com.guildlite.team.entity.TeamUserEntity
import java.time.LocalDateTime
import java.util.UUID

data class TeamResponse(

    val id: UUID,
    val name: String?,
    val description: String?,
    val createdBy: UUID?,
    val teamCoinBalance: Long? = 0,
    val currentMembersCount: Int,
    val createdAt: LocalDateTime,
    val members: List<TeamMemberInfo> = emptyList()
)

data class TeamMemberInfo(
    val userId: UUID,
    val username: String,
    val role: TeamUserEntity.TeamRole,
    val joinedAt: LocalDateTime,
    var addedCoinsCount: Any = 0,
)
