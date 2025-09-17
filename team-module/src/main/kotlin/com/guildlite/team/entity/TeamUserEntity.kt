package com.guildlite.team.entity

import com.guildlite.user.entity.UserEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Version
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "team_users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id"], name = "uk_team_users_active_user")
    ]
)
data class TeamUserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    var id: UUID? = null,

    @Version
    @Column(name = "version")
    val version: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: TeamEntity,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: UserEntity,

    @Enumerated(value = EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: TeamRole = TeamRole.MEMBER,

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false)
    val joinedAt: LocalDateTime? = null,

) {

    enum class TeamRole {
        OWNER, MEMBER
    }

}
