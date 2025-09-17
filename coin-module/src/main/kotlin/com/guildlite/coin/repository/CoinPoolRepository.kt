package com.guildlite.coin.repository

import com.guildlite.coin.entity.CoinPoolEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CoinPoolRepository : JpaRepository<CoinPoolEntity, UUID> {

    @Query("SELECT cp FROM CoinPoolEntity cp WHERE cp.team.id = :teamId")
    fun findByTeamId(@Param("teamId") teamId: UUID): CoinPoolEntity?

    @Query("SELECT cp.balance FROM CoinPoolEntity cp WHERE cp.team.id = :teamId")
    fun getTeamCoinBalance(@Param("teamId") teamId: UUID): Long?

}