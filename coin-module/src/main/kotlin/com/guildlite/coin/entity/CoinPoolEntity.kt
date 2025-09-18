package com.guildlite.coin.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "coin_pools")
data class CoinPoolEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(name = "team_id", nullable = false)
    val teamId: UUID? = null,

    @Column(name = "balance", nullable = false)
    var balance: Long = 0L,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null,

    @Version
    @Column(name = "version")
    var version: Long = 0L
) {


    fun addCoins(amount: Long) {
        require(amount > 0) { "Amount must be positive" }
        balance += amount
    }
}
