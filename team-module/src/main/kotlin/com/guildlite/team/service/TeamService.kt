package com.guildlite.team.service

import com.guildlite.chat.dto.ChatMessage
import com.guildlite.chat.dto.events.TeamEventsDTO
import com.guildlite.chat.publisher.ChatEventPublisher
import com.guildlite.coin.service.CoinService
import com.guildlite.security.dto.UserPrincipal
import com.guildlite.security.provider.JwtTokenProvider
import com.guildlite.team.dto.request.CreateTeamRequest
import com.guildlite.team.dto.response.CreateTeamResponse
import com.guildlite.team.dto.response.JoinTeamResponse
import com.guildlite.team.dto.response.TeamMemberInfo
import com.guildlite.team.dto.response.TeamResponse
import com.guildlite.team.entity.TeamEntity
import com.guildlite.team.entity.TeamUserEntity
import com.guildlite.team.repository.TeamRepository
import com.guildlite.team.repository.TeamUserRepository
import com.guildlite.user.entity.UserEntity
import com.guildlite.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional(readOnly = true)
class TeamService(
    private val teamRepository: TeamRepository,
    private val teamUserRepository: TeamUserRepository,
    private val userRepository: UserRepository,
    private val coinService: CoinService,
    private val chatEventPublisher: ChatEventPublisher,
    private val jwtTokenProvider: JwtTokenProvider
) {

    private val logger = LoggerFactory.getLogger(TeamService::class.java)


    @Transactional
    fun createTeam(request: CreateTeamRequest, userId: UUID): CreateTeamResponse {
        logger.info("Creating Team: {} by user: {}", request.name, userId)

        if (teamUserRepository.findByUserId(userId) != null) {
            return CreateTeamResponse(
                success = false,
                message = "You are already a member of a team."
            )
        }

        val user = userRepository.findById(userId).orElse(null) ?: return CreateTeamResponse(
            success = false,
            message = "User not found."
        )

        var team = TeamEntity(
            name = request.name,
            description = request.description,
            createdAt = LocalDateTime.now(),
            createdBy = userId,
        )

        team = teamRepository.save(team)

        coinService.initializeCoinPool(team)

        val teamUser = TeamUserEntity(
            team = team,
            user = user,
            role = TeamUserEntity.TeamRole.OWNER,
            joinedAt = LocalDateTime.now()
        )

        teamUserRepository.save(teamUser)

        logger.info("Team created successfully: {} by user: {}", team.id, user.id)

        val newToken = generateTokenAfterTeamAction(user, team.id!!)

        return CreateTeamResponse(
            success = true,
            newToken = newToken,
            team = mapToTeamResponse(team),
            message = "Team ${team.name} created successfully"
        )
    }

    @Transactional
    fun joinTeam(teamId: UUID, userId: UUID): JoinTeamResponse {
        logger.info("User {} attempting to join team {}", userId, teamId)

        val existingMembership = teamUserRepository.findByUserId(userId)
        if (existingMembership != null) {
            return JoinTeamResponse(
                success = false,
                message = "You are already a member of team ${existingMembership.team.name}."
            )
        }

        val team = findById(teamId)
        val user = findUserById(userId)
        val currentMembers = teamRepository.countTeamEntitiesById(teamId)

        val teamUser = TeamUserEntity(
            team = team,
            user = user,
            role = TeamUserEntity.TeamRole.MEMBER
        )

        teamUserRepository.save(teamUser)

        val newToken = generateTokenAfterTeamAction(user, teamId)

        logger.info("User {} joined team {} successfully", userId, teamId)

        val teamEventDTO = TeamEventsDTO().apply {
            this.type = ChatMessage.MessageType.TEAM_JOIN
            this.username = user.username
            this.teamId = teamId.toString()
            this.userId = userId.toString()

        }

        chatEventPublisher.publishTeamEvents(teamEventDTO)

        return JoinTeamResponse(
            success = true,
            newToken = newToken,
            team = mapToTeamResponse(team),
            memberCount = currentMembers + 1,
            message = "Successfully joined team ${team.name}"
        )
    }

    @Transactional
    fun leaveTeam(userId: UUID): Map<String, Any> {
        val membership = teamUserRepository.findByUserId(userId)
            ?: return mapOf(
                "success" to false,
                "message" to "You are not a member of any team"
            )

        val teamId = membership.team.id!!
        val teamName = membership.team.name
        val isOwner = membership.role == TeamUserEntity.TeamRole.OWNER

        if (isOwner) {
            val otherMembers = teamUserRepository.findByTeamId(teamId)
                .filter { it.user.id != userId }

            if (otherMembers.isNotEmpty()) {
                val newOwner = otherMembers
                    .filter { it.role == TeamUserEntity.TeamRole.MEMBER }
                    .minByOrNull { it.joinedAt!! }

                if (newOwner != null) {
                    val promotedOwner = newOwner.copy(role = TeamUserEntity.TeamRole.OWNER)
                    teamUserRepository.save(promotedOwner)

                    logger.info(
                        "User {} promoted to OWNER of team {} after owner {} left",
                        newOwner.user.id, teamId, userId
                    )
                }
            }
        }

        teamUserRepository.deleteById(membership.id!!)
        logger.info("User {} left team {}", userId, teamId)

        val remainingMembers = teamUserRepository.findByTeamId(teamId).size

        if (remainingMembers == 0) {
            teamUserRepository.deleteAll(teamUserRepository.findByTeamId(teamId))

            teamRepository.deleteById(teamId)
            logger.info("Team {} deleted - no members remaining", teamId)
        }

        val user = findUserById(userId)
        val newToken = generateTokenAfterTeamAction(user, null)

        val message = when {
            remainingMembers == 0 -> "Successfully left team '$teamName'. Team was disbanded as no members remaining."
            isOwner && remainingMembers > 0 -> {
                val newOwnerName = teamUserRepository.findByTeamId(teamId)
                    .find { it.role == TeamUserEntity.TeamRole.OWNER }?.user?.username
                "Successfully left team '$teamName'. Leadership transferred to $newOwnerName."
            }

            else -> "Successfully left team '$teamName'"
        }

        val teamEventDTO = TeamEventsDTO().apply {
            this.type = ChatMessage.MessageType.TEAM_LEAVE
            this.username = user.username
            this.teamId = teamId.toString()
            this.userId = userId.toString()

        }

        chatEventPublisher.publishTeamEvents(teamEventDTO)

        return mapOf(
            "success" to true,
            "newToken" to newToken,
            "message" to message,
            "teamDisbanded" to (remainingMembers == 0),
            "ownershipTransferred" to (isOwner && remainingMembers > 0)
        )
    }


    fun getTeam(teamId: UUID): TeamResponse {
        val team = findById(teamId)
        return mapToTeamResponse(team)
    }

    private fun findById(teamId: UUID): TeamEntity {
        return teamRepository.findById(teamId).orElse(null)
    }

    private fun findUserById(userId: UUID): UserEntity {
        return userRepository.findById(userId).orElse(null)
    }

    private fun generateTokenAfterTeamAction(user: UserEntity, teamId: UUID?): String {
        val userPrincipal = UserPrincipal.builder()
            .id(user.id!!)
            .username(user.username)
            .teamId(teamId)
            .build()

        return jwtTokenProvider.generateToken(userPrincipal)
    }

    private fun mapToTeamResponse(team: TeamEntity): TeamResponse {
        val memberCount = teamRepository.countTeamEntitiesById(team.id!!)
        val members = teamUserRepository.findByTeamId(team.id!!)
            .map { teamUser ->
                TeamMemberInfo(
                    userId = teamUser.user.id!!,
                    username = teamUser.user.username,
                    role = teamUser.role,
                    joinedAt = teamUser.joinedAt!!,
                    addedCoinsCount = coinService.getTeamCoinsCountAddedByMember(teamUser.user.id!!, team.id!!)
                )
            }

        return TeamResponse(
            id = team.id!!,
            name = team.name,
            description = team.description,
            createdBy = team.createdBy,
            teamCoinBalance = coinService.getTeamCoinBalance(team.id!!),
            currentMembersCount = memberCount,
            createdAt = team.createdAt,
            members = members
        )
    }

}