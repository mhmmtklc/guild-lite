package com.guildlite.coin.dto.response

import java.time.LocalDateTime
import java.util.*

data class CoinBalanceResponse(
    val teamId: UUID? = null,
    val balance: Long? = null,
    val lastUpdated: LocalDateTime? = null,
    val success: Boolean,
    val message: String
)