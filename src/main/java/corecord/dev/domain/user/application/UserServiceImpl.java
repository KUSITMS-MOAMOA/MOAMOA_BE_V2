package corecord.dev.domain.user.application;

import corecord.dev.common.util.RedisLockUtil;
import corecord.dev.domain.ability.application.AbilityDbService;
import corecord.dev.domain.analysis.application.AnalysisDbService;
import corecord.dev.domain.auth.domain.entity.RefreshToken;
import corecord.dev.domain.auth.domain.repository.RefreshTokenRepository;
import corecord.dev.domain.auth.exception.TokenException;
import corecord.dev.domain.auth.jwt.JwtUtil;
import corecord.dev.domain.auth.status.TokenErrorStatus;
import corecord.dev.domain.chat.application.ChatDbService;
import corecord.dev.domain.chat.application.ChatService;
import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.feedback.application.FeedbackDbService;
import corecord.dev.domain.folder.application.FolderDbService;
import corecord.dev.domain.folder.application.FolderService;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.record.application.RecordDbService;
import corecord.dev.domain.record.application.RecordService;
import corecord.dev.domain.user.domain.converter.UserConverter;
import corecord.dev.domain.user.domain.dto.request.UserRequest;
import corecord.dev.domain.user.domain.dto.response.UserResponse;
import corecord.dev.domain.user.domain.enums.Provider;
import corecord.dev.domain.user.domain.enums.Status;
import corecord.dev.domain.user.domain.entity.User;
import corecord.dev.domain.user.exception.UserException;
import corecord.dev.domain.user.status.UserErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AnalysisDbService analysisDbService;
    private final AbilityDbService abilityDbService;
    private final ChatDbService chatDbService;
    private final UserDbService userDbService;
    private final FolderDbService folderDbService;
    private final RecordDbService recordDbService;
    private final FeedbackDbService feedbackDbService;
    private final ChatService chatService;
    private final RecordService recordService;
    private final FolderService folderService;
    private final RedisLockUtil redisLockUtil;

    /**
     * 회원가입
     *
     * @param registerToken
     * @param userRegisterDto
     * @return
     */
    @Override
    @Transactional
    public UserResponse.UserDto registerUser(String registerToken, UserRequest.UserRegisterDto userRegisterDto) {
        String providerId = jwtUtil.getProviderIdFromToken(registerToken);
        Provider provider = extractProviderFromToken(registerToken);

        // 5초간 동일한 providerId로 회원가입 시도 방지
        String lockKey = "register:" + providerId;
        if (!redisLockUtil.acquireLock(lockKey, 5)) {
            throw new UserException(UserErrorStatus.ALREADY_EXIST_USER);
        }

        try {
            validRegisterToken(registerToken);
            validateUserInfo(userRegisterDto.getNickName());
            checkExistUser(providerId, provider);

            // 새로운 유저 생성
            User newUser = UserConverter.toUserEntity(userRegisterDto, providerId, provider);
            User savedUser = userDbService.saveUser(newUser);

            // RefreshToken 생성 및 저장
            String refreshToken = jwtUtil.generateRefreshToken(savedUser.getUserId());
            String accessToken = jwtUtil.generateAccessToken(savedUser.getUserId());
            saveRefreshToken(refreshToken, savedUser);

            // 가이드용 채팅 경험 기록 생성
            ChatRoom chatRoom = chatService.createExampleChat(savedUser);
            Folder folder = folderService.createExampleFolder(savedUser);
            recordService.createExampleRecord(savedUser, folder, chatRoom);

            return UserConverter.toUserDto(savedUser, accessToken, refreshToken);
        } finally {
            // 락 해제
            redisLockUtil.releaseLock(lockKey);
        }
    }

    private Provider extractProviderFromToken(String registerToken) {
        String provider = jwtUtil.getProviderFromToken(registerToken);
        switch (provider) {
            case "GOOGLE":
                return Provider.GOOGLE;
            case "KAKAO":
                return Provider.KAKAO;
            case "NAVER":
                return Provider.NAVER;
            default:
                throw new UserException(UserErrorStatus.INVALID_OUATH2_PROVIDER);
        }
    }

    private void validRegisterToken(String registerToken) {
        if (!jwtUtil.isRegisterTokenValid(registerToken))
            throw new TokenException(TokenErrorStatus.INVALID_REGISTER_TOKEN);
    }

    private void checkExistUser(String providerId, Provider provider) {
        if (userDbService.existsByProviderIdAndProvider(providerId, provider))
            throw new UserException(UserErrorStatus.ALREADY_EXIST_USER);
    }

    private void saveRefreshToken(String refreshToken, User user) {
        RefreshToken newRefreshToken = RefreshToken.of(refreshToken, user.getUserId());
        refreshTokenRepository.save(newRefreshToken);
    }

    /**
     * 로그아웃
     *
     * @param refreshToken
     */
    @Override
    @Transactional
    public void logoutUser(String refreshToken) {
        deleteRefreshTokenInRedis(refreshToken);
    }

    /**
     * 회원 탈퇴
     *
     * @param userId
     * @param refreshToken
     */
    @Override
    @Transactional
    public void deleteUser(Long userId, String refreshToken) {
        // 연관된 데이터 삭제
        feedbackDbService.deleteFeedbackByUserId(userId);
        abilityDbService.deleteAbilityByUserId(userId);
        analysisDbService.deleteAnalysisByUserId(userId);
        chatDbService.deleteChatByUserId(userId);
        recordDbService.deleteRecordByUserId(userId);
        chatDbService.deleteChatRoomByUserId(userId);
        folderDbService.deleteFolderByUserId(userId);
        userDbService.deleteUserByUserId(userId);
        deleteRefreshTokenInRedis(refreshToken);
    }

    private void deleteRefreshTokenInRedis(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.info("쿠키에 리프레쉬 토큰 없음");
            return;
        }
        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findByRefreshToken(refreshToken);
        refreshTokenOptional.ifPresent(refreshTokenRepository::delete);
    }

    /**
     * 유저 정보 수정
     *
     * @param userId
     * @param userUpdateDto
     */
    @Override
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

    /**
     * 유저 정보 조회
     *
     * @param userId
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse.UserInfoDto getUserInfo(Long userId) {
        User user = userDbService.getUser(userId);

        int recordCount = recordDbService.getRecordCount(user.getUserId());;
        return UserConverter.toUserInfoDto(user, recordCount);
    }

}
