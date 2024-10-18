package corecord.dev.domain.auth.application;

import corecord.dev.common.util.CookieUtil;
import corecord.dev.common.util.JwtUtil;
import corecord.dev.domain.auth.dto.KakaoUserInfo;
import corecord.dev.domain.auth.dto.OAuth2UserInfo;
import corecord.dev.domain.token.entity.RefreshToken;
import corecord.dev.domain.token.repository.RefreshTokenRepository;
import corecord.dev.domain.user.entity.User;
import corecord.dev.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Value("${jwt.redirect.access}")
    private String ACCESS_TOKEN_REDIRECT_URI;

    @Value("${jwt.redirect.register}")
    private String REGISTER_TOKEN_REDIRECT_URI;

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2UserInfo oAuth2UserInfo = new KakaoUserInfo(token.getPrincipal().getAttributes());

        String providerId = oAuth2UserInfo.getProviderId();
        String name = oAuth2UserInfo.getName();
        log.info("providerId: {}", providerId);
        log.info("name: {}", name);

        Optional<User> optionalUser = userRepository.findByProviderId(providerId);

        if (optionalUser.isPresent()) {
            handleExistingUser(request, response, optionalUser.get());
        } else {
            handleNewUser(request, response, providerId);
        }
    }

    // 기존 유저 처리
    private void handleExistingUser(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        log.info("기존 유저입니다. 액세스 토큰과 리프레쉬 토큰을 발급합니다.");

        // 기존 리프레쉬 토큰 삭제
        refreshTokenRepository.deleteByUserId(user.getUserId());

        // 새로운 리프레쉬 토큰 생성 및 저장
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());
        saveRefreshToken(user, refreshToken);

        // 기존 쿠키 삭제
        deleteExistingTokens(response);

        // RefreshToken 쿠키 추가
        ResponseCookie refreshTokenCookie = cookieUtil.createTokenCookie("refreshToken", refreshToken);
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        // AccessToken 쿠키 추가
        String accessToken = jwtUtil.generateAccessToken(user.getUserId());
        ResponseCookie accessTokenCookie = cookieUtil.createTokenCookie("accessToken", accessToken);
        response.addHeader("Set-Cookie", accessTokenCookie.toString());

        String redirectURI = String.format(ACCESS_TOKEN_REDIRECT_URI, accessToken, refreshToken);

        // 액세스 토큰 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, redirectURI);
    }

    // 신규 유저 처리
    private void handleNewUser(HttpServletRequest request, HttpServletResponse response, String providerId) throws IOException {
        log.info("신규 유저입니다. 레지스터 토큰을 발급합니다.");

        // 이미 레지스터 토큰이 있다면 삭제
        response.addCookie(cookieUtil.deleteCookie("registerToken"));

        // 레지스터 토큰 발급
        String registerToken = jwtUtil.generateRegisterToken(providerId);

        // 레지스터 토큰 쿠키 설정
        ResponseCookie registerTokenCookie = cookieUtil.createTokenCookie("registerToken", registerToken);
        response.addHeader("Set-Cookie", registerTokenCookie.toString());

        String redirectURI = String.format(REGISTER_TOKEN_REDIRECT_URI, registerToken);

        // 레지스터 토큰 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, redirectURI);
    }

    // 리프레쉬 토큰 저장
    private void saveRefreshToken(User user, String refreshToken) {
        RefreshToken newRefreshToken = RefreshToken.builder()
                .userId(user.getUserId())
                .refreshToken(refreshToken)
                .build();
        refreshTokenRepository.save(newRefreshToken);
    }

    // 기존 토큰 삭제
    private void deleteExistingTokens(HttpServletResponse response) {
        response.addCookie(cookieUtil.deleteCookie("accessToken"));
        response.addCookie(cookieUtil.deleteCookie("refreshToken"));
    }
}
