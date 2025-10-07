package com.mmc.bookduck.global.oauth2;

import com.mmc.bookduck.global.security.CookieUtil;
import com.mmc.bookduck.global.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private static final String OAUTH_PATH = "/api/oauth";
    private static final String DEPLOYED_REDIRECT_URL = "https://bookduck.co.kr";
    private static final List<String> ALLOWED_REDIRECT_URLS = List.of(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://localhost:8080",
            "https://mmc-fe2.vercel.app"
    );

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            // 요청의 origin 또는 referer 헤더를 사용해 출처 확인
            String origin = request.getHeader("origin");
            String referer = request.getHeader("referer");

            String baseRedirectUrl = ALLOWED_REDIRECT_URLS.stream()
                    .filter(url -> url.equals(origin) || (referer != null && referer.startsWith(url)))
                    .findFirst()
                    .orElse(DEPLOYED_REDIRECT_URL);
            String redirectUrl = baseRedirectUrl + OAUTH_PATH;

            // 액세스 토큰 및 리프레시 토큰 발급, 리프레시 토큰을 쿠키에 저장
            String accessToken = jwtUtil.generateAccessToken(authentication);
            int expiresIn = jwtUtil.getAccessTokenMaxAge();
            String refreshToken = jwtUtil.generateRefreshToken(authentication);
            cookieUtil.addCookie(response, "refreshToken", refreshToken, jwtUtil.getRefreshTokenMaxAge());

            // OAuth2UserDetails으로부터 신규 유저 여부와 userId 확인
            OAuth2UserDetails userDetails = (OAuth2UserDetails) authentication.getPrincipal();
            boolean isNewUser = userDetails.isNewUser();
            Long userId = userDetails.userId();

            log.info("소셜 로그인 {}에 성공하였습니다. 발급된 accessToken: {}", isNewUser ? "회원 가입" : "로그인", accessToken);

            // 액세스 토큰을 쿼리 파라미터로 전달하여 리디렉션
            String redirectUrlWithParams = UriComponentsBuilder.fromUriString(redirectUrl)
                    .queryParam("accessToken", accessToken)
                    .queryParam("expiresIn", expiresIn)
                    .queryParam("isNewUser", isNewUser)
                    .queryParam("userId", userId)
                    .build()
                    .toUriString();
            response.sendRedirect(redirectUrlWithParams);
        } catch (Exception e) {
            log.error("소셜 로그인 실패: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "소셜 로그인 실패");
        }
    }
}
