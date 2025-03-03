package corecord.dev.domain.auth.application;

import corecord.dev.domain.auth.jwt.JwtUtil;
import corecord.dev.domain.auth.domain.entity.RefreshToken;
import corecord.dev.domain.auth.domain.entity.TmpToken;
import corecord.dev.domain.auth.status.TokenErrorStatus;
import corecord.dev.domain.auth.exception.TokenException;
import corecord.dev.domain.auth.domain.repository.RefreshTokenRepository;
import corecord.dev.domain.auth.domain.repository.TmpTokenRepository;
import corecord.dev.domain.user.application.UserDbService;
import corecord.dev.domain.user.domain.converter.UserConverter;
import corecord.dev.domain.user.domain.dto.response.UserResponse;
import corecord.dev.domain.user.domain.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final UserDbService userDbService;
    private final TmpTokenRepository tmpTokenRepository;

    /**
     * 임시 토큰을 이용하여 AccessToken과 RefreshToken을 발급한다.
     * @param tmpToken
     * @return
     */
    @Transactional
    public UserResponse.UserDto issueTokens(String tmpToken) {
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

        User user = userDbService.findUserById(userId);
        return UserConverter.toUserDto(user, accessToken, refreshToken);
    }

    /**
     * RefreshToken을 이용하여 새로운 AccessToken을 발급한다.
     * @param refreshToken
     * @return
     */
    @Transactional
    public String reissueAccessToken(String refreshToken) {
        validateRefreshToken(refreshToken);

        Long userId = Long.parseLong(jwtUtil.getUserIdFromRefreshToken(refreshToken));

        return jwtUtil.generateAccessToken(userId);
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
}
