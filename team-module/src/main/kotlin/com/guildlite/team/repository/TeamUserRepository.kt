package com.guildlite.team.repository

import com.guildlite.team.entity.TeamUserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TeamUserRepository : JpaRepository<TeamUserEntity, UUID> {

    @Query("SELECT tu FROM TeamUserEntity tu WHERE tu.user.id = :userId")
    fun findByUserId(@Param("userId") userId: UUID): TeamUserEntity?

    @Query("SELECT tu FROM TeamUserEntity tu WHERE tu.team.id = :teamId ORDER BY tu.joinedAt")
    fun findByTeamId(@Param("teamId") teamId: UUID): List<TeamUserEntity>


}