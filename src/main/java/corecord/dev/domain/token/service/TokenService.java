package corecord.dev.domain.token.service;

import corecord.dev.common.util.CookieUtil;
import corecord.dev.common.util.JwtUtil;
import corecord.dev.domain.token.converter.TokenConverter;
import corecord.dev.domain.token.dto.response.TokenResponse;
import corecord.dev.domain.token.entity.RefreshToken;
import corecord.dev.domain.token.entity.TmpToken;
import corecord.dev.domain.token.exception.enums.TokenErrorStatus;
import corecord.dev.domain.token.exception.model.TokenException;
import corecord.dev.domain.token.repository.RefreshTokenRepository;
import corecord.dev.domain.token.repository.TmpTokenRepository;
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
    private final TmpTokenRepository tmpTokenRepository;

    @Transactional
    public TokenResponse.AccessTokenResponse issueTokens(HttpServletResponse response, String tmpToken) {
        // 임시 토큰 유효성 검증
        TmpToken tmpTokenEntity = validateTmpToken(tmpToken);
        tmpTokenRepository.delete(tmpTokenEntity);

        // 새 RefreshToken 발급 및 저장
        Long userId = Long.parseLong(jwtUtil.getUserIdFromTmpToken(tmpToken));
        String refreshToken = jwtUtil.generateRefreshToken(userId);
        RefreshToken newRefreshToken = RefreshToken.of(refreshToken, userId);
        refreshTokenRepository.save(newRefreshToken);

        // 쿠키에 RefreshToken 추가
        setCookie(response, "refreshToken", refreshToken);

        // 새 AccessToken 발급
        String accessToken = jwtUtil.generateAccessToken(userId);
        return TokenConverter.toAccessTokenResponse(accessToken);
    }

    @Transactional
    public TokenResponse.AccessTokenResponse reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromCookie(request);

        // RefreshToken 유효성 검증
        validateRefreshToken(refreshToken);

        // 새 AccessToken 발급
        Long userId = Long.parseLong(jwtUtil.getUserIdFromRefreshToken(refreshToken));
        String newAccessToken = jwtUtil.generateAccessToken(userId);

        // 쿠키에 AccessToken 추가
        setCookie(response, "accessToken", newAccessToken);
        return TokenConverter.toAccessTokenResponse(newAccessToken);
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        String refreshToken = cookieUtil.getCookieValue(request, "refreshToken");
        if (refreshToken == null) {
            throw new TokenException(TokenErrorStatus.REFRESH_TOKEN_NOT_FOUND);
        }
        return refreshToken;
    }

    private void validateRefreshToken(String refreshToken) {
        if (!jwtUtil.isRefreshTokenValid(refreshToken)) {
            throw new TokenException(TokenErrorStatus.INVALID_REFRESH_TOKEN);
        }

        refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new TokenException(TokenErrorStatus.REFRESH_TOKEN_NOT_FOUND));

    }

    private TmpToken validateTmpToken(String tmpToken) {
        if (!jwtUtil.isTmpTokenValid(tmpToken)) {
            throw new TokenException(TokenErrorStatus.INVALID_TMP_TOKEN);
        }
        return tmpTokenRepository.findByTmpToken(tmpToken)
                .orElseThrow(() -> new TokenException(TokenErrorStatus.TMP_TOKEN_NOT_FOUND));
    }

    private void setCookie(HttpServletResponse response, String tokenName, String tokenValue) {
        ResponseCookie cookie = cookieUtil.createTokenCookie(tokenName, tokenValue);
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
