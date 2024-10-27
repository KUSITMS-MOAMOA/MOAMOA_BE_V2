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
import corecord.dev.domain.user.exception.enums.UserErrorStatus;
import corecord.dev.domain.user.exception.model.UserException;
import corecord.dev.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public UserResponse.UserRegisterDto registerUser(HttpServletResponse response, String registerToken, UserRequest.UserRegisterDto userRegisterDto) {
        // registerToken 유효성 검증
        validRegisterToken(registerToken);

        // user 정보 유효성 검증
        validateUserRegisterInfo(userRegisterDto);

        // 새로운 유저 생성
        String providerId = jwtUtil.getProviderIdFromToken(registerToken);
        User newUser = UserConverter.toUserEntity(userRegisterDto, providerId);
        User savedUser = userRepository.save(newUser);

        // RefreshToken 생성 및 저장
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getUserId());
        saveRefreshToken(refreshToken, savedUser);

        // 새 RefreshToken 쿠키 설정
        setTokenCookies(response, refreshToken, savedUser);
        return UserConverter.toUserRegisterDto(savedUser, jwtUtil.generateAccessToken(savedUser.getUserId()));
    }

    // 로그아웃
    public void logoutUser(HttpServletRequest request, HttpServletResponse response, Long userId) {
        // Redis 안에 RefreshToken 삭제
        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findByRefreshToken(cookieUtil.getCookieValue(request, "refreshToken"));
        refreshTokenOptional.ifPresent(refreshTokenRepository::delete);

        // RefreshToken 쿠키 삭제
        ResponseCookie refreshTokenCookie = cookieUtil.deleteCookie("refreshToken");
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
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
    }

    // user 정보 유효성 검증
    private void validateUserRegisterInfo(UserRequest.UserRegisterDto userRegisterDto) {
        String nickName = userRegisterDto.getNickName();
        if (nickName == null || nickName.isEmpty() || nickName.length() > 10) {
            throw new UserException(UserErrorStatus.INVALID_USER_NICKNAME);
        }

        // 한글, 영어, 숫자, 공백만 허용
        String nicknamePattern = "^[a-zA-Z0-9가-힣\\s]*$";
        if (!Pattern.matches(nicknamePattern, nickName)) {
            throw new UserException(UserErrorStatus.INVALID_USER_NICKNAME);
        }
    }
}
