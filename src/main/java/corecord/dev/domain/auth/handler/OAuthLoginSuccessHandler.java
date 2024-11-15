package corecord.dev.domain.auth.handler;

import corecord.dev.common.util.CookieUtil;
import corecord.dev.domain.auth.util.JwtUtil;
import corecord.dev.domain.auth.dto.KakaoUserInfo;
import corecord.dev.domain.auth.dto.OAuth2UserInfo;
import corecord.dev.domain.auth.entity.RefreshToken;
import corecord.dev.domain.auth.entity.TmpToken;
import corecord.dev.domain.auth.repository.RefreshTokenRepository;
import corecord.dev.domain.auth.repository.TmpTokenRepository;
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
    private final TmpTokenRepository tmpTokenRepository;

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
        log.info("기존 유저입니다. 임시 토큰을 발급합니다.");
        // 기존 리프레쉬 토큰, 쿠키 삭제
        deleteByUserId(user.getUserId());
        ResponseCookie refreshTokenCookie = cookieUtil.deleteCookie("refreshToken");
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        // 액세스 토큰 쿠키 삭제
        ResponseCookie accessTokenCookie = cookieUtil.deleteCookie("accessToken");
        response.addHeader("Set-Cookie", accessTokenCookie.toString());

        // 임시 토큰 생성
        String tmpToken = jwtUtil.generateTmpToken(user.getUserId());
        tmpTokenRepository.save(TmpToken.of(tmpToken, user.getUserId()));
        String redirectURI = String.format(ACCESS_TOKEN_REDIRECT_URI, tmpToken);

        getRedirectStrategy().sendRedirect(request, response, redirectURI);
    }

    // 신규 유저 처리
    private void handleNewUser(HttpServletRequest request, HttpServletResponse response, String providerId) throws IOException {
        log.info("신규 유저입니다. 레지스터 토큰을 발급합니다.");
        // 레지스터 토큰 발급
        String registerToken = jwtUtil.generateRegisterToken(providerId);
        String redirectURI = String.format(REGISTER_TOKEN_REDIRECT_URI, registerToken);
        getRedirectStrategy().sendRedirect(request, response, redirectURI);
    }

    private void deleteByUserId(Long userId) {
        Iterable<RefreshToken> tokens = refreshTokenRepository.findAll();
        for (RefreshToken token : tokens) {
            if (token.getUserId().equals(userId)) {
                refreshTokenRepository.delete(token);
            }
        }
    }
}
