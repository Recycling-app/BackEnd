package com.example.recycling_app.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key secretKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration.access}") long accessTokenExpirationMs,
            @Value("${jwt.expiration.refresh}") long refreshTokenExpirationMs
    ) {
        // 시크릿 키 길이는 최소 32 바이트 이상이어야 합니다.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(String uid, String email) {
        return createToken(uid, email, accessTokenExpirationMs);
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(String uid, String email) {
        return createToken(uid, email, refreshTokenExpirationMs);
    }

    /**
     * 토큰 생성 로직 - 공통 내부 메서드
     */
    private String createToken(String uid, String email, long expirationTimeMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTimeMs);

        return Jwts.builder()
                .setSubject(uid)
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException |
                 ExpiredJwtException | UnsupportedJwtException |
                 IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 토큰에서 uid (subject) 추출
     */
    public String extractUid(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰에서 email claim 추출
     */
    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    /**
     * Claims 추출 내부 메서드
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

