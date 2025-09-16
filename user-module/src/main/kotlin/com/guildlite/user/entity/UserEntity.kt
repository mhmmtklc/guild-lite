package com.guildlite.user.entity

import jakarta.persistence.*
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["username"], name = "uk_users_username"),
        UniqueConstraint(columnNames = ["username"], name = "uk_users_username")
    ]
)
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID? = UUID.randomUUID(),

    @Column(name = "username", nullable = false, length = 30)
    val username: String,

    @Column(name = "password", nullable = false)
    val password: String,

    @Column(name = "email", nullable = false, length = 100)
    val email: String,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime? = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = LocalDateTime.now(),

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: UserStatus = UserStatus.ACTIVE,
) {

    constructor() : this(
        username = "",
        password = "",
        email = ""
    )

    fun isActive(): Boolean = status == UserStatus.ACTIVE

    fun updateLastLoginTime() {
        lastLoginAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }


    enum class UserStatus {
        ACTIVE, INACTIVE, DELETED
    }
}
