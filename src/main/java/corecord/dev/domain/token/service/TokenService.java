package corecord.dev.domain.token.service;

import corecord.dev.common.util.CookieUtil;
import corecord.dev.common.util.JwtUtil;
import corecord.dev.domain.token.converter.TokenConverter;
import corecord.dev.domain.token.dto.response.TokenResponse;
import corecord.dev.domain.token.entity.RefreshToken;
import corecord.dev.domain.token.exception.enums.TokenErrorStatus;
import corecord.dev.domain.token.exception.model.TokenException;
import corecord.dev.domain.token.repository.RefreshTokenRepository;
import corecord.dev.domain.user.exception.model.UserException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    public void test(HttpServletResponse response, String registerToken) {
        // registerToken 유효성 검증
        if(!jwtUtil.isRegisterTokenValid(registerToken)) {
            throw new TokenException(TokenErrorStatus.INVALID_REGISTER_TOKEN);
        }

        // registerToken에서 providerId 추출
        String providerId = jwtUtil.getProviderIdFromToken(registerToken);
        log.info("providerId: {}", providerId);

        // 배포환경 쿠키 발급 테스트
        String tmpRefreshToken = "000tmpRefreshToken000";
        ResponseCookie tmpRefreshTokenCookie = cookieUtil.createTokenCookie("tmpRefreshToken", tmpRefreshToken);

        // 쿠키 생성
        response.addHeader("Set-Cookie", tmpRefreshTokenCookie.toString());
    }

    @Transactional
    public TokenResponse.AccessTokenResponse reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        // RefreshToken 추출 및 유효성 검증
        String refreshToken = getRefreshTokenFromCookie(request);

        // RefreshToken이 유효한지 확인
        Long userId = Long.parseLong(jwtUtil.getUserIdFromRefreshToken(refreshToken));
        validateRefreshToken(refreshToken);

        // 새 AccessToken 발급
        String newAccessToken = jwtUtil.generateAccessToken(userId);

        // AccessToken 쿠키 생성
        setAccessTokenCookie(response, newAccessToken);

        return TokenConverter.toAccessTokenResponse(newAccessToken);
    }

    // 쿠키에서 RefreshToken 가져오기
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        String refreshToken = cookieUtil.getCookieValue(request, "refreshToken");
        if (refreshToken == null) {
            throw new TokenException(TokenErrorStatus.REFRESH_TOKEN_NOT_FOUND);
        }
        return refreshToken;
    }

    // RefreshToken 검증
    private void validateRefreshToken(String refreshToken) {
        RefreshToken existingRefreshToken = getExistingRefreshToken(refreshToken);
        if (!existingRefreshToken.getRefreshToken().equals(refreshToken) || !jwtUtil.isRefreshTokenValid(refreshToken)) {
            throw new TokenException(TokenErrorStatus.INVALID_REFRESH_TOKEN);
        }
    }

    // DB에서 RefreshToken 확인
    private RefreshToken getExistingRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new TokenException(TokenErrorStatus.REFRESH_TOKEN_NOT_FOUND));
    }

    // AccessToken 쿠키 생성 및 응답 헤더에 추가
    private void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
        ResponseCookie accessTokenCookie = cookieUtil.createTokenCookie("accessToken", accessToken);
        response.addHeader("Set-Cookie", accessTokenCookie.toString());
    }
}
