package com.guildlite.coin.service

import com.guildlite.chat.dto.ChatMessage
import com.guildlite.chat.dto.events.CoinEventsDTO
import com.guildlite.chat.publisher.ChatEventPublisher
import com.guildlite.coin.dto.request.AddCoinsRequest
import com.guildlite.coin.dto.response.AddCoinsResponse
import com.guildlite.coin.dto.response.CoinBalanceResponse
import com.guildlite.coin.entity.CoinPoolEntity
import com.guildlite.coin.entity.CoinTransactionEntity
import com.guildlite.coin.repository.CoinPoolRepository
import com.guildlite.coin.repository.CoinTransactionRepository
import com.guildlite.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class CoinService(
    private val coinPoolRepository: CoinPoolRepository,
    private val coinTransactionRepository: CoinTransactionRepository,
    private val userRepository: UserRepository,
    private val chatEventPublisher: ChatEventPublisher
) {

    private val logger = LoggerFactory.getLogger(CoinService::class.java)

    @Transactional
    fun addCoins(teamId: UUID, request: AddCoinsRequest, userId: UUID): AddCoinsResponse {
        logger.info("Adding {} coins to team {} by user {}", request.amount, teamId, userId)

        val user = findUserById(userId)

        val coinPool = coinPoolRepository.findByTeamId(teamId)
            ?: createCoinPool(teamId)

        val oldBalance = coinPool.balance

        coinPool.addCoins(request.amount)
        coinPoolRepository.save(coinPool)

        val transaction = CoinTransactionEntity(
            coinPool = coinPool,
            user = user,
            amount = request.amount,
            type = CoinTransactionEntity.TransactionType.ADD,
            description = request.description
        )
        coinTransactionRepository.save(transaction)

        logger.info("Added {} coins to team {}. Balance: {} -> {}",
            request.amount, teamId, oldBalance, coinPool.balance)

        val coinEventDTO = CoinEventsDTO().apply {
            this.type = ChatMessage.MessageType.COIN_ADD
            this.username = user.username
            this.teamId = teamId.toString()
            this.userId = userId.toString()
            this.amount = request.amount
            this.newBalance = coinPool.balance
        }

        chatEventPublisher.publishCoinEvents(coinEventDTO)

        return AddCoinsResponse(
            success = true,
            amountAdded = request.amount,
            newBalance = coinPool.balance,
            message = "Successfully added ${request.amount} coins to team pool"
        )
    }

    fun getCoinBalance(teamId: UUID): CoinBalanceResponse {
        val coinPool = coinPoolRepository.findByTeamId(teamId)

        return CoinBalanceResponse(
            teamId = teamId,
            balance = coinPool?.balance ?: 0L,
            lastUpdated = coinPool?.updatedAt,
            success = true,
            message = "Successfully retrieved balance for team pool"
        )
    }

    @Transactional
    fun initializeCoinPool(teamId: UUID): CoinPoolEntity {
        logger.info("Initializing coin pool for team {}", teamId)

        val coinPool = CoinPoolEntity(teamId = teamId)
        return coinPoolRepository.save(coinPool)
    }

    private fun createCoinPool(teamId: UUID): CoinPoolEntity {
        logger.info("Creating new coin pool for team {}", teamId)
        val coinPool = CoinPoolEntity(teamId = teamId)
        return coinPoolRepository.save(coinPool)
    }

    private fun findUserById(userId: UUID): com.guildlite.user.entity.UserEntity {
        return userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found: $userId") }
    }

    fun getTeamCoinBalance(teamId: UUID): Long {
        return coinPoolRepository.getTeamCoinBalance(teamId) ?: 0L
    }

    fun getTeamCoinsCountAddedByMember(userId: UUID, teamId: UUID): Long {
        return coinTransactionRepository.getTeamCoinCountAddedByMember(userId, teamId) ?: 0L
    }
}