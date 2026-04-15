package com.test.oes.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getJwt().getSecret()));
    }

    public String generateAccessToken(LoginUser loginUser) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.getJwt().getAccessTokenMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .issuer(jwtProperties.getJwt().getIssuer())
                .subject(loginUser.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim("uid", loginUser.getUserId())
                .claim("role", loginUser.getAccountRole().getAuthority())
                .claim("accountType", loginUser.getAccountRole().name())
                .claim("displayName", loginUser.getDisplayName())
                .claim("tokenType", "access")
                .signWith(secretKey)
                .compact();
    }

    public RefreshTokenPayload generateRefreshToken(LoginUser loginUser) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.getJwt().getRefreshTokenDays(), ChronoUnit.DAYS);
        String jti = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .issuer(jwtProperties.getJwt().getIssuer())
                .subject(loginUser.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .id(jti)
                .claim("uid", loginUser.getUserId())
                .claim("role", loginUser.getAccountRole().getAuthority())
                .claim("tokenType", "refresh")
                .signWith(secretKey)
                .compact();
        return new RefreshTokenPayload(token, jti, expiresAt);
    }

    public Claims parseClaims(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return claims.getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Token 无效或已过期", e);
        }
    }

    public LoginUser parseAccessToken(String token) {
        Claims claims = parseClaims(token);
        if (!"access".equals(claims.get("tokenType", String.class))) {
            throw new IllegalArgumentException("非法 accessToken");
        }
        AccountRole role = AccountRole.valueOf(claims.get("accountType", String.class));
        return new LoginUser(
                claims.get("uid", Integer.class),
                claims.getSubject(),
                claims.get("displayName", String.class),
                role,
                null
        );
    }

    public RefreshTokenClaims parseRefreshToken(String token) {
        Claims claims = parseClaims(token);
        if (!"refresh".equals(claims.get("tokenType", String.class))) {
            throw new IllegalArgumentException("非法 refreshToken");
        }
        return new RefreshTokenClaims(
                claims.getId(),
                claims.getSubject(),
                claims.get("uid", Integer.class),
                claims.get("role", String.class),
                claims.getExpiration().toInstant()
        );
    }

    public long getAccessTokenExpiresInSeconds() {
        return jwtProperties.getJwt().getAccessTokenMinutes() * 60;
    }

    public record RefreshTokenPayload(String token, String jti, Instant expiresAt) {
    }

    public record RefreshTokenClaims(String jti, String username, Integer userId, String role, Instant expiresAt) {
    }
}
