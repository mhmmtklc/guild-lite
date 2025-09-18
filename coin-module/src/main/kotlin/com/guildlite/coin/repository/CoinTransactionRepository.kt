package com.guildlite.coin.repository

import com.guildlite.coin.entity.CoinTransactionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CoinTransactionRepository : JpaRepository<CoinTransactionEntity, UUID> {

    @Query(value = "SELECT SUM(ct.amount) FROM CoinTransactionEntity ct WHERE ct.user.id = :userId AND ct.coinPool.teamId = :teamId")
    fun getTeamCoinCountAddedByMember(@Param("userId") userId: UUID, @Param("teamId") teamId: UUID): Long?
}