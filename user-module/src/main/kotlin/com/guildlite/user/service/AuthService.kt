package com.guildlite.user.service

import com.guildlite.security.dto.UserPrincipal
import com.guildlite.security.provider.JwtTokenProvider
import com.guildlite.user.dto.request.LoginRequest
import com.guildlite.user.dto.request.RegisterRequest
import com.guildlite.user.dto.response.LoginResponse
import com.guildlite.user.entity.UserEntity
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


    @Transactional
    fun login(loginRequest: LoginRequest): LoginResponse {
        logger.info("Login attempt for: {}", loginRequest.usernameOrEmail)

        val user = userService.findByUsernameOrEmail(loginRequest.usernameOrEmail)
            ?: return LoginResponse(
                success = false,
                message = "Invalid username/email or password"
            )

        if (!user.isActive()) {
            return LoginResponse(
                success = false,
                message = "Account status is ${user.status}, please contact the administrator for activation'"
            )
        }

        if (!passwordEncoder.matches(loginRequest.password, user.password)) {
            logger.warn("Invalid password attempt for user: {}", user.username)
            return LoginResponse(
                success = false,
                message = "Invalid username/email or password"
            )
        }


        userService.updateLastLogin(user.id!!)

        val userPrincipal = UserPrincipal.builder()
            .id(user.id)
            .username(user.username)
            .teamId(null)
            .build()

        val token = jwtTokenProvider.generateToken(userPrincipal)

        logger.info("Successful login for user: {}", user.username)

        return LoginResponse(
            token = token,
            user = userService.mapToUserResponse(user),
            success = true,
            message = "Successful login for user: ${user.username}"
        )
    }

    @Transactional
    fun register(registerRequest: RegisterRequest): LoginResponse {
        logger.info("Registration attempt for username: {}", registerRequest.username)

        if (userService.existsByUsername(registerRequest.username)) {
            return LoginResponse(
                success = false,
                message = "Username '${registerRequest.username}' is already taken"
            )
        }

        if (userService.existsByEmail(registerRequest.email)) {
            return LoginResponse(
                success = false,
                message = "Email '${registerRequest.email}' is already registered"
            )
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
            .teamId(savedUser.currentTeamId)
            .build()

        val token = jwtTokenProvider.generateToken(userPrincipal)

        logger.info("Successful registration for user: {}", savedUser.username)

        return LoginResponse(
            token = token,
            user = userService.mapToUserResponse(savedUser),
            success = true,
            message = "Successful registration for user: ${savedUser.username}"
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