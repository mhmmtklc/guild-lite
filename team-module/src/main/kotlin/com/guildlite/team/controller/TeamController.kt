package com.guildlite.team.controller

import com.guildlite.security.dto.UserPrincipal
import com.guildlite.team.dto.request.CreateTeamRequest
import com.guildlite.team.dto.response.CreateTeamResponse
import com.guildlite.team.dto.response.JoinTeamResponse
import com.guildlite.team.dto.response.TeamResponse
import com.guildlite.team.service.TeamService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/teams")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Team Management", description = "Team management processes controller")
class TeamController(
    private val teamService: TeamService
) {

    @PostMapping("/create")
    fun createTeam(@Valid @RequestBody request: CreateTeamRequest, @AuthenticationPrincipal userPrincipal: UserPrincipal): ResponseEntity<CreateTeamResponse> {
        val response = teamService.createTeam(request, userPrincipal.id)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/{teamId}/join")
    fun joinTeam(@PathVariable teamId: UUID, @AuthenticationPrincipal userPrincipal: UserPrincipal): ResponseEntity<JoinTeamResponse> {
        val response = teamService.joinTeam(teamId, userPrincipal.id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{teamId}/get")
    fun getTeam(@PathVariable teamId: UUID): ResponseEntity<TeamResponse> {
        val team = teamService.getTeam(teamId)
        return ResponseEntity.ok(team)
    }


    // TODO
    //@DeleteMapping("/leave")
    fun leaveTeam(@AuthenticationPrincipal userPrincipal: UserPrincipal): ResponseEntity<Map<String, Any>> {
        val result = teamService.leaveTeam(userPrincipal.id)
        return ResponseEntity.ok(result)
    }


}