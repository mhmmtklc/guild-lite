package com.guildlite.team.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "teams")
data class TeamEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    var id: UUID? = null,

    @Version
    @Column(name = "version")
    val version: Long = 0L,

    @Column(name = "created_by", nullable = false)
    val createdBy: UUID? = null,

    @Column(name = "name", nullable = false, length = 50)
    val name: String? = null,

    @Column(name = "description", nullable = false, length = 150)
    val description: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val members: MutableSet<TeamUserEntity> = mutableSetOf()

)