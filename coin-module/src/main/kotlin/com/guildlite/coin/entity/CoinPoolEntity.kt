package com.guildlite.coin.entity

import com.guildlite.team.entity.TeamEntity
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false, unique = true)
    val team: TeamEntity,

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
    constructor() : this(team = TeamEntity())

    fun addCoins(amount: Long) {
        require(amount > 0) { "Amount must be positive" }
        balance += amount
    }
}
