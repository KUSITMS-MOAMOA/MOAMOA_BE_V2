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
import java.net.URLEncoder;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Value("${jwt.redirect.access}")
    private String ACCESS_TOKEN_REDIRECT_URI; // 기존 유저 로그인 시 리다이렉트 URI

    @Value("${jwt.redirect.register}")
    private String REGISTER_TOKEN_REDIRECT_URI; // 신규 유저 로그인 시 리다이렉트 URI

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
        User user;

        if (optionalUser.isPresent()) {
            log.info("기존 유저입니다. 액세스 토큰과 리프레쉬 토큰을 발급합니다.");
            user = optionalUser.get();
            refreshTokenRepository.deleteByUserId(user.getUserId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());
            RefreshToken newRefreshToken = RefreshToken.builder().userId(user.getUserId()).refreshToken(refreshToken).build();
            refreshTokenRepository.save(newRefreshToken);

            ResponseCookie cookie = cookieUtil.createRefreshTokenCookie(refreshToken);
            response.addHeader("Set-Cookie", cookie.toString());

            String accessToken = URLEncoder.encode(jwtUtil.generateAccessToken(user.getUserId()));
            String redirectURI = String.format(ACCESS_TOKEN_REDIRECT_URI, accessToken);
            getRedirectStrategy().sendRedirect(request, response, redirectURI);
        } else {
            log.info("신규 유저입니다. 레지스터 토큰을 발급합니다.");
            String registerToken = URLEncoder.encode(jwtUtil.generateRegisterToken(providerId));
            String redirectURI = String.format(REGISTER_TOKEN_REDIRECT_URI, registerToken);
            getRedirectStrategy().sendRedirect(request, response, redirectURI);
        }
    }


}
