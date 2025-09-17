package com.guildlite.coin.entity

import com.guildlite.user.entity.UserEntity
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "coin_transactions")
data class CoinTransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_pool_id", nullable = false)
    val coinPool: CoinPoolEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Column(name = "amount", nullable = false)
    val amount: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: TransactionType,

    @Column(name = "description")
    val description: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime? = null
) {
    constructor() : this(
        coinPool = CoinPoolEntity(),
        user = UserEntity(),
        amount = 0L,
        type = TransactionType.ADD
    )

    enum class TransactionType {
        ADD
    }
}
