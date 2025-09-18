package com.guildlite.coin.controller

import com.guildlite.coin.dto.request.AddCoinsRequest
import com.guildlite.coin.dto.response.AddCoinsResponse
import com.guildlite.coin.dto.response.CoinBalanceResponse
import com.guildlite.coin.service.CoinService
import com.guildlite.security.dto.UserPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/coins")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Coin Management", description = "Team coin pool management controller")
class CoinController(
    private val coinService: CoinService
) {


    @Operation(
        summary = "Add Coins",
        description = "Add coins to team pool and broadcast coin event to team chat"
    )
    @PostMapping("/{teamId}/add")
    fun addCoins(
        @PathVariable teamId: UUID, @Valid @RequestBody request: AddCoinsRequest,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<AddCoinsResponse> {
        if (teamId != userPrincipal.teamId) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                AddCoinsResponse(
                    success = false,
                    message = "You are not authorized to perform this action."
                )
            )
        }

        val response = coinService.addCoins(teamId, request, userPrincipal.id)
        return ResponseEntity.ok(response)
    }


    @Operation(
        summary = "Get Coin Balance",
        description = "Retrieve current coin balance for team"
    )
    @GetMapping("/{teamId}/get-balance")
    fun getCoinBalance(
        @PathVariable teamId: UUID,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<CoinBalanceResponse> {
        if (teamId != userPrincipal.teamId) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                CoinBalanceResponse(
                    success = false,
                    message = "You are not authorized to perform this action."
                )
            )
        }

        val balance = coinService.getCoinBalance(teamId)
        return ResponseEntity.ok(balance)
    }
}