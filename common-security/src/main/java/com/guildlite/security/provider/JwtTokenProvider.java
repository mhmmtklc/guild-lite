package com.guildlite.security.provider;

import com.guildlite.security.dto.UserPrincipal;
import com.guildlite.security.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;


@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String USERNAME_CLAIM = "username";
    private static final String USER_ID_CLAIM = "userId";
    private static final String TEAM_ID_CLAIM = "teamId";

    @Value("${guildlite.jwt.secret}")
    private String jwtSecret;

    @Value("${guildlite.jwt.expiration-hours}")
    private int jwtExpirationHours;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }


    public String generateToken(UserPrincipal userPrincipal) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(jwtExpirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString())
                .claim(USER_ID_CLAIM, userPrincipal.getId().toString())
                .claim(USERNAME_CLAIM, userPrincipal.getUsername())
                .claim(TEAM_ID_CLAIM, userPrincipal.getTeamId() != null ?
                        userPrincipal.getTeamId().toString() : null)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }


    public UUID getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String userIdStr = claims.get(USER_ID_CLAIM, String.class);
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }


    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get(USERNAME_CLAIM, String.class);
    }


    public UUID getTeamIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String teamIdStr = claims.get(TEAM_ID_CLAIM, String.class);
        return teamIdStr != null ? UUID.fromString(teamIdStr) : null;
    }


    public UserPrincipal getUserPrincipalFromToken(String token) {
        Claims claims = getClaimsFromToken(token);

        String userId = claims.get(USER_ID_CLAIM, String.class);
        String username = claims.get(USERNAME_CLAIM, String.class);
        String teamId = claims.get(TEAM_ID_CLAIM, String.class);

        return UserPrincipal.builder()
                .id(UUID.fromString(userId))
                .username(username)
                .teamId(UUID.fromString(teamId))
                .build();
    }


    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            logger.debug("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException ex) {
            return true;
        }
    }

    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            throw new JwtAuthenticationException("JWT token has expired", ex);
        } catch (UnsupportedJwtException ex) {
            throw new JwtAuthenticationException("JWT token is unsupported", ex);
        } catch (MalformedJwtException ex) {
            throw new JwtAuthenticationException("JWT token is malformed", ex);
        } catch (IllegalArgumentException ex) {
            throw new JwtAuthenticationException("JWT token is invalid", ex);
        }
    }
}