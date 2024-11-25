package corecord.dev.domain.user.application;

import corecord.dev.common.util.CookieUtil;
import corecord.dev.domain.ability.application.AbilityDbService;
import corecord.dev.domain.analysis.application.AnalysisDbService;
import corecord.dev.domain.auth.jwt.JwtUtil;
import corecord.dev.domain.chat.application.ChatDbService;
import corecord.dev.domain.folder.application.FolderDbService;
import corecord.dev.domain.record.application.RecordDbService;
import corecord.dev.domain.auth.domain.entity.RefreshToken;
import corecord.dev.domain.auth.status.TokenErrorStatus;
import corecord.dev.domain.auth.exception.TokenException;
import corecord.dev.domain.auth.domain.repository.RefreshTokenRepository;
import corecord.dev.domain.user.domain.converter.UserConverter;
import corecord.dev.domain.user.domain.dto.request.UserRequest;
import corecord.dev.domain.user.domain.dto.response.UserResponse;
import corecord.dev.domain.user.domain.entity.Status;
import corecord.dev.domain.user.domain.entity.User;
import corecord.dev.domain.user.status.UserErrorStatus;
import corecord.dev.domain.user.exception.UserException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AnalysisDbService analysisDbService;
    private final AbilityDbService abilityDbService;
    private final ChatDbService chatDbService;
    private final UserDbService userDbService;
    private final FolderDbService folderDbService;
    private final RecordDbService recordDbService;

    /**
     * 회원가입
     * @param registerToken
     * @param userRegisterDto
     * @return
     */
    @Transactional
    public UserResponse.UserDto registerUser(String registerToken, UserRequest.UserRegisterDto userRegisterDto) {
        validRegisterToken(registerToken);
        validateUserInfo(userRegisterDto.getNickName());

        String providerId = jwtUtil.getProviderIdFromToken(registerToken);

        checkExistUser(providerId);

        // 새로운 유저 생성
        User newUser = UserConverter.toUserEntity(userRegisterDto, providerId);
        User savedUser = userDbService.saveUser(newUser);

        // RefreshToken 생성 및 저장
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getUserId());
        saveRefreshToken(refreshToken, savedUser);

        String accessToken = jwtUtil.generateAccessToken(savedUser.getUserId());

        return UserConverter.toUserDto(savedUser, accessToken, refreshToken);
    }

    /**
     * 로그아웃
     * @param refreshToken
     */
    @Transactional
    public void logoutUser(String refreshToken) {
        deleteRefreshTokenInRedis(refreshToken);
    }

    /**
     * 회원 탈퇴
     * @param userId
     * @param refreshToken
     */
    @Transactional
    public void deleteUser(Long userId, String refreshToken) {
        // 연관된 데이터 삭제
        abilityDbService.deleteAbilityByUserId(userId);
        analysisDbService.deleteAnalysisByUserId(userId);
        chatDbService.deleteChatByUserId(userId);
        recordDbService.deleteRecordByUserId(userId);
        chatDbService.deleteChatRoomByUserId(userId);
        folderDbService.deleteFolderByUserId(userId);
        userDbService.deleteUserByUserId(userId);
        deleteRefreshTokenInRedis(refreshToken);
    }

    /**
     * 유저 정보 수정
     * @param userId
     * @param userUpdateDto
     */
    @Transactional
    public void updateUser(Long userId, UserRequest.UserUpdateDto userUpdateDto) {
        User user = userDbService.getUser(userId);

        if(userUpdateDto.getNickName() != null) {
            validateUserInfo(userUpdateDto.getNickName());
            user.setNickName(userUpdateDto.getNickName());
        }

        if(userUpdateDto.getStatus() != null) {
            user.setStatus(Status.getStatus(userUpdateDto.getStatus()));
        }
    }

    /**
     * 유저 정보 조회
     * @param userId
     * @return
     */
    @Transactional
    public UserResponse.UserInfoDto getUserInfo(Long userId) {
        User user = userDbService.getUser(userId);

        int recordCount = recordDbService.getRecordCount(user);;
        return UserConverter.toUserInfoDto(user, recordCount);
    }

    private void saveRefreshToken(String refreshToken, User user) {
        RefreshToken newRefreshToken = RefreshToken.of(refreshToken, user.getUserId());
        refreshTokenRepository.save(newRefreshToken);
    }

    private void deleteRefreshTokenInRedis(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.info("쿠키에 리프레쉬 토큰 없음");
            return;
        }
        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findByRefreshToken(refreshToken);
        refreshTokenOptional.ifPresent(refreshTokenRepository::delete);
    }

    private void checkExistUser(String providerId) {
        if (userDbService.IsUserExistByProviderId(providerId)) {
            throw new UserException(UserErrorStatus.ALREADY_EXIST_USER);
        }
    }

    private void validateUserInfo(String nickName) {
        if (nickName == null || nickName.isEmpty() || nickName.length() > 10) {
            throw new UserException(UserErrorStatus.INVALID_USER_NICKNAME);
        }

        // 한글, 영어, 숫자, 공백만 허용
        String nicknamePattern = "^[a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ가-힣\s]*$";
        if (!Pattern.matches(nicknamePattern, nickName)) {
            throw new UserException(UserErrorStatus.INVALID_USER_NICKNAME);
        }
    }

    private void validRegisterToken(String registerToken) {
        if (!jwtUtil.isRegisterTokenValid(registerToken)) {
            throw new TokenException(TokenErrorStatus.INVALID_REGISTER_TOKEN);
        }
    }
}
