package com.guildlite.team.repository

import com.guildlite.team.entity.TeamEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TeamRepository : JpaRepository<TeamEntity, UUID> {

    fun countTeamEntitiesById(@Param("teamId") teamId: UUID): Int
}