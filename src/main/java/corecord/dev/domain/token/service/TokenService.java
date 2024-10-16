package corecord.dev.domain.token.service;

import corecord.dev.common.util.CookieUtil;
import corecord.dev.common.util.JwtUtil;
import corecord.dev.domain.token.dto.response.TokenResponse;
import corecord.dev.domain.token.entity.RefreshToken;
import corecord.dev.domain.token.exception.enums.TokenErrorStatus;
import corecord.dev.domain.token.exception.model.TokenException;
import corecord.dev.domain.token.repository.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;
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
        Cookie cookie = cookieUtil.getCookie(request);
        String refreshToken = cookie.getValue();
        Long userId = Long.parseLong(jwtUtil.getUserIdFromRefreshToken(refreshToken));
        RefreshToken existRefreshToken = getExistRefreshToken(refreshToken);
        if (!existRefreshToken.getRefreshToken().equals(refreshToken) || !(jwtUtil.isAccessTokenValid(refreshToken))) {
            throw new TokenException(TokenErrorStatus.INVALID_REFRESH_TOKEN);
        }
        String newAccessToken = jwtUtil.generateAccessToken(userId);
        ResponseCookie newCookie = cookieUtil.createRefreshTokenCookie(refreshToken);
        response.addHeader("Set-Cookie", newCookie.toString());
        return TokenResponse.AccessTokenResponse.builder().accessToken(newAccessToken).build();
    }

    private RefreshToken getExistRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new TokenException(TokenErrorStatus.REFRESH_TOKEN_NOT_FOUND));
    }
}
