package com.guildlite.user.service

import com.guildlite.user.dto.response.UserResponse
import com.guildlite.user.entity.UserEntity
import com.guildlite.user.exception.UserNotFoundException
import com.guildlite.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)



    @Transactional
    fun save(user: UserEntity): UserEntity {
        val savedUser = userRepository.save(user)
        logger.info("User Saved: {}", savedUser.username)
        return savedUser
    }

    @Transactional
    fun updateLastLogin(userId: UUID?) {
        if (userId == null) {
            throw IllegalArgumentException("User ID cannot be null")
        }

        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException("Invalid user ID: $userId") }
        user.updateLastLogin()
        logger.debug("Updated last login for user: {}", userId)
    }



    fun findByUsernameOrEmail(usernameOrEmail: String): UserEntity? {
        return userRepository.findByUsernameOrEmail(usernameOrEmail).orElse(null)
    }


    fun existsByUsername(username: String): Boolean {
        return userRepository.existsByUsernameIgnoreCase(username)
    }


    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmailIgnoreCase(email)
    }

    fun mapToUserResponse(user: UserEntity): UserResponse {
        return UserResponse(
            id = user.id.toString(),
            username = user.username,
            email = user.email,
            status = user.status,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt

        )
    }
}