package com.guildlite.coin.dto.response


data class AddCoinsResponse(

    val success: Boolean,
    val amountAdded: Long? = null,
    val newBalance: Long? = null,
    val message: String
)