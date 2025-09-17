package com.guildlite.coin.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class AddCoinsRequest(

    @field:NotNull(message = "Amount is required")
    @field:Min(value = 1, message = "Amount must be at least 1")
    @field:Positive(message = "Amount must be positive")
    val amount: Long,

    val description: String? = null
)