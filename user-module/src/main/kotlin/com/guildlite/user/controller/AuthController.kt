package com.guildlite.user.controller

import com.guildlite.user.dto.request.LoginRequest
import com.guildlite.user.dto.request.RegisterRequest
import com.guildlite.user.dto.response.LoginResponse
import com.guildlite.user.service.AuthService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth Controller", description = "User auth processes controller")
class AuthController(
    private val authService: AuthService
) {

    private val logger = LoggerFactory.getLogger(AuthController::class.java)


    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        logger.info("Login request received for: {}", loginRequest.usernameOrEmail)
        val loginResponse = authService.login(loginRequest)
        return ResponseEntity.ok(loginResponse)
    }


    @PostMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): ResponseEntity<LoginResponse> {
        logger.info("Registration request received for username: {}", registerRequest.username)

        val loginResponse = authService.register(registerRequest)

        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse)
    }
}