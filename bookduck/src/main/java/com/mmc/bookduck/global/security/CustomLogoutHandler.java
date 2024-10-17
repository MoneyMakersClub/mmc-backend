package com.mmc.bookduck.global.security;

import com.mmc.bookduck.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final CookieUtil cookieUtil;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null && authentication.getName() != null){
            // Authorization 헤더에서 JWT 토큰을 가져오기
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);
                jwtUtil.validateAccessToken(accessToken);

                try {
                    // 리프레시 토큰을 Redis에서 삭제 (authentication.getName()인 email을 key로 찾음)
                    redisService.deleteValues(authentication.getName());
                    // 리프레시 토큰 쿠키 삭제
                } catch (RedisConnectionFailureException e) {
                    log.error("Redis 연결에 실패했습니다.");
                }
            }
            // 쿠키 삭제
            cookieUtil.deleteCookie(response, "refreshToken");
            
            // 기본 로그아웃 핸들러 기능 수행
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(request, response, authentication);
        } else {
            ErrorResponseUtil.writeErrorResponse(response, ErrorCode.NOT_AUTHENTICATED, request.getRequestURI());
        }
    }
}