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
    public UserResponse.UserRegisterDto registerUser(HttpServletResponse response, String authorizationHeader, UserRequest.UserRegisterDto request) {
        String registerToken = jwtUtil.getTokenFromHeader(authorizationHeader);

        if(!jwtUtil.isRegisterTokenValid(registerToken)) {
            throw new TokenException(TokenErrorStatus.INVALID_REGISTER_TOKEN);
        }

        String providerId = jwtUtil.getProviderIdFromToken(registerToken);
        User newUser = UserConverter.toUserEntity(request, providerId);
        User user = userRepository.save(newUser);

        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());
        RefreshToken newRefreshToken = RefreshToken.builder().userId(user.getUserId()).refreshToken(refreshToken).build();
        refreshTokenRepository.save(newRefreshToken);

        ResponseCookie cookie = cookieUtil.createRefreshTokenCookie(refreshToken);
        response.addHeader("Set-Cookie", cookie.toString());

        String accessToken = jwtUtil.generateAccessToken(user.getUserId());
        return UserConverter.toUserRegisterDto(user, accessToken);
    }

}
