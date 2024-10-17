package corecord.dev.domain.user.service;

import corecord.dev.common.util.CookieUtil;
import corecord.dev.common.util.JwtUtil;
import corecord.dev.domain.token.entity.RefreshToken;
import corecord.dev.domain.token.exception.enums.TokenErrorStatus;
import corecord.dev.domain.token.exception.model.TokenException;
import corecord.dev.domain.token.repository.RefreshTokenRepository;
import corecord.dev.domain.user.converter.UserConverter;
import corecord.dev.domain.user.dto.request.UserRequest;
import corecord.dev.domain.user.dto.response.UserResponse;
import corecord.dev.domain.user.entity.User;
import corecord.dev.domain.user.repository.UserRepository;
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
public class UserService {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public UserResponse.UserRegisterDto registerUser(HttpServletResponse response, HttpServletRequest request, UserRequest.UserRegisterDto userRegisterDto) {
        // 쿠키에서 registerToken 가져오기
        String registerToken = getRegisterTokenFromCookie(request);

        // registerToken 유효성 검증
        validRegisterToken(registerToken);

        // 새로운 유저 생성
        String providerId = jwtUtil.getProviderIdFromToken(registerToken);
        User newUser = UserConverter.toUserEntity(userRegisterDto, providerId);
        User savedUser = userRepository.save(newUser);

        // RefreshToken 생성 및 저장
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getUserId());
        saveRefreshToken(refreshToken, savedUser);

        // registerToken 쿠키 삭제
        addDeleteCookieHeader(response, "registerToken");

        // 새 RefreshToken 및 AccessToken 쿠키 설정
        setTokenCookies(response, refreshToken, savedUser);

        return UserConverter.toUserRegisterDto(savedUser, jwtUtil.generateAccessToken(savedUser.getUserId()));
    }

    // 쿠키에서 registerToken 가져오기
    private String getRegisterTokenFromCookie(HttpServletRequest request) {
        return cookieUtil.getCookieValue(request, "registerToken")
                .orElseThrow(() -> new TokenException(TokenErrorStatus.REGISTER_TOKEN_NOT_FOUND));
    }

    // registerToken 유효성 검증
    private void validRegisterToken(String registerToken) {
        if (!jwtUtil.isRegisterTokenValid(registerToken)) {
            throw new TokenException(TokenErrorStatus.INVALID_REGISTER_TOKEN);
        }
    }

    // RefreshToken 저장
    private void saveRefreshToken(String refreshToken, User user) {
        RefreshToken newRefreshToken = RefreshToken.of(refreshToken, user.getUserId());
        refreshTokenRepository.save(newRefreshToken);
    }

    // 토큰 쿠키 설정
    private void setTokenCookies(HttpServletResponse response, String refreshToken, User user) {
        // RefreshToken 쿠키 추가
        ResponseCookie refreshTokenCookie = cookieUtil.createTokenCookie("refreshToken", refreshToken);
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        // AccessToken 쿠키 추가
        String accessToken = jwtUtil.generateAccessToken(user.getUserId());
        ResponseCookie accessTokenCookie = cookieUtil.createTokenCookie("accessToken", accessToken);
        response.addHeader("Set-Cookie", accessTokenCookie.toString());
    }

    // 쿠키 삭제 헤더 추가
    private void addDeleteCookieHeader(HttpServletResponse response, String cookieName) {
        response.addCookie(cookieUtil.deleteCookie(cookieName));
    }
}
