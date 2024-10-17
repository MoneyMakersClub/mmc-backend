package com.mmc.bookduck.domain.auth.dto.response;

public record TokenResponseDto(
        String accessToken,
        String refreshToken,
        long accessTokenMaxAge,
        long refreshTokenMaxAge
) {
}
