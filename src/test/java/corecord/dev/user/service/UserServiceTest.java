package corecord.dev.user.service;

import corecord.dev.domain.auth.domain.repository.RefreshTokenRepository;
import corecord.dev.domain.auth.jwt.JwtUtil;
import corecord.dev.domain.record.application.RecordDbService;
import corecord.dev.domain.user.application.UserDbService;
import corecord.dev.domain.user.domain.dto.request.UserRequest;
import corecord.dev.domain.user.domain.dto.response.UserResponse;
import corecord.dev.domain.user.domain.enums.Status;
import corecord.dev.domain.user.domain.entity.User;
import corecord.dev.domain.user.status.UserErrorStatus;
import corecord.dev.domain.user.exception.UserException;
import corecord.dev.domain.user.application.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDbService userDbService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RecordDbService recordDbService;

    @InjectMocks
    private UserService userService;

    private static final String REGISTER_TOKEN = "validRegisterToken";
    private static final String PROVIDER_ID = "1234567890";
    private static final String REFRESH_TOKEN = "generatedRefreshToken";
    private static final String ACCESS_TOKEN = "generatedAccessToken";
    private User newUser;

    @BeforeEach
    void setUp() {
        newUser = createTestUser();
    }

//    @Test
//    @DisplayName("유저 회원가입 테스트")
//    void registerUser() {
//        // Given
//        UserRequest.UserRegisterDto userRegisterDto = new UserRequest.UserRegisterDto();
//        userRegisterDto.setNickName("testUser");
//        userRegisterDto.setStatus("대학생");
//
//        when(jwtUtil.isRegisterTokenValid(REGISTER_TOKEN)).thenReturn(true);
//        when(jwtUtil.getProviderIdFromToken(REGISTER_TOKEN)).thenReturn(PROVIDER_ID);
//        when(jwtUtil.generateRefreshToken(anyLong())).thenReturn(REFRESH_TOKEN);
//        when(jwtUtil.generateAccessToken(anyLong())).thenReturn(ACCESS_TOKEN);
//        when(userDbService.IsUserExistByProviderId(PROVIDER_ID)).thenReturn(false);
//        when(userDbService.saveUser(any(User.class))).thenReturn(newUser);
//
//        // When
//        UserResponse.UserDto userDto = userService.registerUser(REGISTER_TOKEN, userRegisterDto);
//
//        // Then
//        assertThat(userDto.getNickname()).isEqualTo(newUser.getNickName());
//        assertThat(userDto.getStatus()).isEqualTo(newUser.getStatus().getValue());
//    }

    @Test
    @DisplayName("회원 정보 조회 테스트")
    void getUserInfo() {
        // Given
        when(userDbService.getUser(newUser.getUserId())).thenReturn(newUser);
        when(recordDbService.getRecordCount(newUser.getUserId())).thenReturn(0);

        // When
        UserResponse.UserInfoDto userInfoDto = userService.getUserInfo(newUser.getUserId());

        // Then
        assertThat(userInfoDto.getNickname()).isEqualTo(newUser.getNickName());
        assertThat(userInfoDto.getStatus()).isEqualTo(newUser.getStatus().getValue());
    }

    @Test
    @DisplayName("회원 정보 수정 테스트")
    void updateUser() {
        // Given
        UserRequest.UserUpdateDto updateDto = new UserRequest.UserUpdateDto();
        updateDto.setNickName("editName");
        updateDto.setStatus("인턴");

        when(userDbService.getUser(newUser.getUserId())).thenReturn(newUser);

        // When
        userService.updateUser(newUser.getUserId(), updateDto);

        // Then
        assertThat(newUser.getNickName()).isEqualTo("editName");
        assertThat(newUser.getStatus()).isEqualTo(Status.INTERN);
        verify(userDbService).getUser(newUser.getUserId());
    }

    @Test
    @DisplayName("닉네임 유효성 검증 - 닉네임이 길이 초과일 때 예외 발생")
    void validateUserInfo_NickNameExceedsLength_ThrowsUserException() {
        // Given
        String invalidNickName = "VeryLongNickname";
        UserRequest.UserRegisterDto userRegisterDto = new UserRequest.UserRegisterDto();
        userRegisterDto.setNickName(invalidNickName);
        userRegisterDto.setStatus("대학생");

        when(jwtUtil.isRegisterTokenValid(REGISTER_TOKEN)).thenReturn(true);

        // When & Then
        UserException exception = Assertions.assertThrows(UserException.class,
                () -> userService.registerUser(REGISTER_TOKEN, userRegisterDto));
        assertThat(exception.getErrorStatus()).isEqualTo(UserErrorStatus.INVALID_USER_NICKNAME);
    }

    @Test
    @DisplayName("닉네임 유효성 검증 - 닉네임에 허용되지 않는 문자가 포함된 경우 예외 발생")
    void validateUserInfo_NickNameHasInvalidCharacters_ThrowsUserException() {
        // Given
        String invalidNickName = "Invalid@Nickname!";
        UserRequest.UserRegisterDto userRegisterDto = new UserRequest.UserRegisterDto();
        userRegisterDto.setNickName(invalidNickName);
        userRegisterDto.setStatus("대학생");

        when(jwtUtil.isRegisterTokenValid(REGISTER_TOKEN)).thenReturn(true);

        // When & Then
        UserException exception = Assertions.assertThrows(UserException.class,
                () -> userService.registerUser(REGISTER_TOKEN, userRegisterDto));
        assertThat(exception.getErrorStatus()).isEqualTo(UserErrorStatus.INVALID_USER_NICKNAME);
    }

    private User createTestUser() {
        return User.builder()
                .userId(1L)
                .providerId(UserServiceTest.PROVIDER_ID)
                .nickName("testUser")
                .status(Status.UNIVERSITY_STUDENT)
                .build();
    }
}
