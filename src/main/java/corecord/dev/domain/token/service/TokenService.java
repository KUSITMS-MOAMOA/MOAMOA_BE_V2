package corecord.dev.domain.token.service;

import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.common.util.CookieUtil;
import corecord.dev.common.util.JwtUtil;
import corecord.dev.domain.token.entity.RefreshToken;
import corecord.dev.domain.token.entity.TmpToken;
import corecord.dev.domain.token.exception.enums.TokenErrorStatus;
import corecord.dev.domain.token.exception.model.TokenException;
import corecord.dev.domain.token.repository.RefreshTokenRepository;
import corecord.dev.domain.token.repository.TmpTokenRepository;
import corecord.dev.domain.user.converter.UserConverter;
import corecord.dev.domain.user.dto.response.UserResponse;
import corecord.dev.domain.user.entity.User;
import corecord.dev.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final TmpTokenRepository tmpTokenRepository;

    @Value("${jwt.access-token.expiration-time}")
    private long accessTokenExpirationTime;

    @Value("${jwt.refresh-token.expiration-time}")
    private long refreshTokenExpirationTime;

    /**
     * 임시 토큰을 이용하여 AccessToken과 RefreshToken을 발급한다.
     * @param response
     * @param tmpToken
     * @return
     */
    @Transactional
    public UserResponse.UserDto issueTokens(HttpServletResponse response, String tmpToken) {
        // 임시 토큰 유효성 검증
        TmpToken tmpTokenEntity = validateTmpToken(tmpToken);
        tmpTokenRepository.delete(tmpTokenEntity);

        // 새 RefreshToken 발급 및 저장
        Long userId = Long.parseLong(jwtUtil.getUserIdFromTmpToken(tmpToken));
        String refreshToken = jwtUtil.generateRefreshToken(userId);
        RefreshToken newRefreshToken = RefreshToken.of(refreshToken, userId);
        refreshTokenRepository.save(newRefreshToken);

        // 새 AccessToken 발급
        String accessToken = jwtUtil.generateAccessToken(userId);

        // 쿠키에 AccessToken 및 RefreshToken 추가
        setTokenCookies(response, "refreshToken", refreshToken);
        setTokenCookies(response, "accessToken", accessToken);

        User user = findUserById(userId);
        return UserConverter.toUserDto(user);
    }

    /**
     * RefreshToken을 이용하여 새로운 AccessToken을 발급한다.
     * @param request
     * @param response
     * @return
     */
    @Transactional
    public UserResponse.UserDto reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromCookie(request);

        // RefreshToken 유효성 검증
        validateRefreshToken(refreshToken);

        // 새 AccessToken 발급
        Long userId = Long.parseLong(jwtUtil.getUserIdFromRefreshToken(refreshToken));
        String newAccessToken = jwtUtil.generateAccessToken(userId);

        // 기존 AccessToken 삭제
        ResponseCookie accessTokenCookie = cookieUtil.deleteCookie("accessToken");
        response.addHeader("Set-Cookie", accessTokenCookie.toString());

        // 쿠키에 새 AccessToken 추가
        setTokenCookies(response, "accessToken", newAccessToken);

        User user = findUserById(userId);
        return UserConverter.toUserDto(user);
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

    // 토큰 쿠키 설정
    private void setTokenCookies(HttpServletResponse response, String tokenName, String token) {
        if(tokenName.equals("accessToken")) {
            ResponseCookie accessTokenCookie = cookieUtil.createTokenCookie(tokenName, token, accessTokenExpirationTime);
            response.addHeader("Set-Cookie", accessTokenCookie.toString());
        } else {
            ResponseCookie refreshTokenCookie = cookieUtil.createTokenCookie(tokenName, token, refreshTokenExpirationTime);
            response.addHeader("Set-Cookie", refreshTokenCookie.toString());
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.UNAUTHORIZED));
    }
}
