package com.mmc.bookduck.domain.auth.controller;

import com.mmc.bookduck.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "auth", description = "Auth 관련 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "액세스 토큰과 리프레시 토큰 재발급",
            description = "만료된 액세스 토큰과 만료되지 않은 리프레시 토큰을 통해 두 토큰 모두의 재발급을 요청합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue("refreshToken") String refreshToken,
                                          @RequestHeader("Authorization") String accessToken) {
        // 리프레시 토큰 검증 및 액세스 토큰과 리프레시 토큰 재발급
        return ResponseEntity.ok(authService.refreshTokens(accessToken, refreshToken));
    }
}
