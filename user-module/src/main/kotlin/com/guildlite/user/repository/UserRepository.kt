package com.guildlite.user.repository

import com.guildlite.user.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {

    @Query(value = "SELECT u FROM UserEntity u " +
            "WHERE " +
            "LOWER(u.username) = LOWER(:usernameOrEmail) " +
            "OR " +
            "LOWER(u.email) = LOWER(:usernameOrEmail) ")
    fun findByUsernameOrEmail(@Param("usernameOrEmail") usernameOrEmail: String): Optional<UserEntity>

    fun existsByUsernameIgnoreCase(username: String): Boolean

    fun existsByEmailIgnoreCase(email: String): Boolean

    @Modifying
    @Query("UPDATE UserEntity u SET u.lastLoginAt = :loginTime, u.updatedAt = :loginTime WHERE u.id = :userId")
    fun updateLastLoginAt(
        @Param("userId") userId: UUID,
        @Param("loginTime") loginTime: LocalDateTime
    )
}