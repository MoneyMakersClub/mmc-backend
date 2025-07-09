package com.mmc.bookduck.domain.auth.service;

import com.mmc.bookduck.domain.auth.dto.response.TokenResponseDto;
import com.mmc.bookduck.global.exception.CustomTokenException;
import com.mmc.bookduck.global.exception.ErrorCode;
import com.mmc.bookduck.global.security.CookieUtil;
import com.mmc.bookduck.global.security.JwtUtil;
import com.mmc.bookduck.global.redis.RedisService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final CookieUtil cookieUtil;

    // 리프레시 토큰 검증 및 액세스 토큰과 리프레시 토큰 재발급
    public TokenResponseDto refreshTokens(String accessToken, String refreshToken, HttpServletResponse response) {
        // 액세스 토큰 만료 여부 확인
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        if (!jwtUtil.isAccessTokenExpired(accessToken)) {
            throw new CustomTokenException(ErrorCode.ACCESS_TOKEN_NOT_EXPIRED);
        }

        // 리프레시 토큰 유효성 검사
        jwtUtil.validateRefreshToken(refreshToken);
        Claims claims = jwtUtil.getRefreshTokenClaims(refreshToken);
        String email = claims.getSubject();
        String redisKey = "auth:refresh_token:" + email;
        // Redis에 저장된 리프레시 토큰과 일치하는지 확인
        Object storedRefreshToken = redisService.getValues(redisKey);

        if (storedRefreshToken == null || !storedRefreshToken.toString().equals(refreshToken)) {
            throw new CustomTokenException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 기존 리프레시 토큰 삭제
        redisService.deleteValues(redisKey);

        // 새 액세스 토큰 및 리프레시 토큰 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, Collections.singletonList(new SimpleGrantedAuthority(claims.get("role").toString())));
        String newAccessToken = jwtUtil.generateAccessToken(authentication);
        String newRefreshToken = jwtUtil.generateRefreshToken(authentication); // Redis에 저장

        // 새 리프레시 토큰을 HttpOnly 쿠키에 저장
        cookieUtil.addCookie(response, "refreshToken", newRefreshToken, jwtUtil.getRefreshTokenMaxAge());

        return new TokenResponseDto(newAccessToken, jwtUtil.getAccessTokenMaxAge());
    }
}
