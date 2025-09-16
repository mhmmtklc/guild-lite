package com.guildlite.user.service

import com.guildlite.user.dto.request.LoginRequest
import com.guildlite.user.dto.request.RegisterRequest
import com.guildlite.user.dto.response.LoginResponse
import com.guildlite.user.entity.UserEntity
import com.guildlite.user.exception.InvalidCredentialsException
import com.guildlite.user.exception.UserAlreadyExistsException
import com.guildlite.security.dto.UserPrincipal
import com.guildlite.security.provider.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)


    fun login(loginRequest: LoginRequest): LoginResponse {
        logger.info("Login attempt for: {}", loginRequest.usernameOrEmail)

        val user = userService.findByUsernameOrEmail(loginRequest.usernameOrEmail)
            ?: throw InvalidCredentialsException("Invalid username/email or password")

        if (user.id == null) {
            throw InvalidCredentialsException("Invalid username/email or password")
        }

        if (!user.isActive()) {
            throw InvalidCredentialsException("Account is not active")
        }

        if (!passwordEncoder.matches(loginRequest.password, user.password)) {
            logger.warn("Invalid password attempt for user: {}", user.username)
            throw InvalidCredentialsException("Invalid username/email or password")
        }


        userService.updateLastLogin(user.id)

        val userPrincipal = UserPrincipal.builder()
            .id(user.id)
            .username(user.username)
            .teamId(null)
            .build()

        val token = jwtTokenProvider.generateToken(userPrincipal)

        logger.info("Successful login for user: {}", user.username)

        return LoginResponse(
            token = token,
            user = userService.mapToUserResponse(user)
        )
    }

    fun register(registerRequest: RegisterRequest): LoginResponse {
        logger.info("Registration attempt for username: {}", registerRequest.username)

        if (userService.existsByUsername(registerRequest.username)) {
            throw UserAlreadyExistsException("Username '${registerRequest.username}' is already taken")
        }

        if (userService.existsByEmail(registerRequest.email)) {
            throw UserAlreadyExistsException("Email '${registerRequest.email}' is already registered")
        }


        val newUser = UserEntity(
            username = registerRequest.username.trim(),
            email = registerRequest.email.trim().lowercase(),
            password = passwordEncoder.encode(registerRequest.password),
            status = UserEntity.UserStatus.ACTIVE
        )

        val savedUser = userService.save(newUser)


        val userPrincipal = UserPrincipal.builder()
            .id(savedUser.id)
            .username(savedUser.username)
            .teamId(null)
            .build()

        val token = jwtTokenProvider.generateToken(userPrincipal)

        logger.info("Successful registration for user: {}", savedUser.username)

        return LoginResponse(
            token = token,
            user = userService.mapToUserResponse(savedUser)
        )
    }

    fun validateToken(token: String): UserPrincipal? {
        return if (jwtTokenProvider.validateToken(token)) {
            jwtTokenProvider.getUserPrincipalFromToken(token)
        } else {
            null
        }
    }

}