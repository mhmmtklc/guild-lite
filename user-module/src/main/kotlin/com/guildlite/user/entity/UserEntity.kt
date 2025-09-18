package com.guildlite.user.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["username"], name = "uk_users_username"),
        UniqueConstraint(columnNames = ["email"], name = "uk_users_email")
    ]
)
data class UserEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    var id: UUID? = null,

    @Version
    @Column(name = "version")
    var version: Long = 0L,

    @Column(name = "username", nullable = false, length = 50)
    var username: String = "",

    @Column(name = "email", nullable = false, length = 100)
    var email: String = "",

    @Column(name = "password", nullable = false)
    var password: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: UserStatus = UserStatus.ACTIVE,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null,

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null,

    @Column(name = "team_id")
    var teamId: UUID? = null

) {

    fun isActive(): Boolean = status == UserStatus.ACTIVE

    fun updateLastLogin() {
        lastLoginAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    enum class UserStatus {
        ACTIVE
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String {
        return "UserEntity(id=$id, " +
                "username='$username', " +
                "email='$email', " +
                "status=$status, " +
                "createdAt=$createdAt, " +
                "updatedAt=$updatedAt, " +
                "lastLoginAt=$lastLoginAt)"
    }
}