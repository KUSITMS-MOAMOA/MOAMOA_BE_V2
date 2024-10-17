package corecord.dev.domain.token.service;

import corecord.dev.common.util.CookieUtil;
import corecord.dev.common.util.JwtUtil;
import corecord.dev.domain.token.dto.response.TokenResponse;
import corecord.dev.domain.token.entity.RefreshToken;
import corecord.dev.domain.token.exception.enums.TokenErrorStatus;
import corecord.dev.domain.token.exception.model.TokenException;
import corecord.dev.domain.token.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

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

        return TokenResponse.AccessTokenResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    // 쿠키에서 RefreshToken 가져오기
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        return cookieUtil.getCookieValue(request, "refreshToken")
                .orElseThrow(() -> new TokenException(TokenErrorStatus.INVALID_REFRESH_TOKEN));
    }

    // RefreshToken 검증
    private void validateRefreshToken(String refreshToken) {
        RefreshToken existingRefreshToken = getExistingRefreshToken(refreshToken);
        if (!existingRefreshToken.getRefreshToken().equals(refreshToken) || !jwtUtil.isAccessTokenValid(refreshToken)) {
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
