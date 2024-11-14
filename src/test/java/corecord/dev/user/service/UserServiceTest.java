package corecord.dev.user.service;

import corecord.dev.common.util.CookieUtil;
import corecord.dev.domain.auth.repository.RefreshTokenRepository;
import corecord.dev.domain.auth.util.JwtUtil;
import corecord.dev.domain.record.repository.RecordRepository;
import corecord.dev.domain.user.dto.request.UserRequest;
import corecord.dev.domain.user.dto.response.UserResponse;
import corecord.dev.domain.user.entity.Status;
import corecord.dev.domain.user.entity.User;
import corecord.dev.domain.user.exception.enums.UserErrorStatus;
import corecord.dev.domain.user.exception.model.UserException;
import corecord.dev.domain.user.repository.UserRepository;
import corecord.dev.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private UserService userService;

    private static final long ACCESS_TOKEN_EXPIRATION = 86400000L;
    private static final long REFRESH_TOKEN_EXPIRATION = 2592000000L;
    private static final String REGISTER_TOKEN = "validRegisterToken";
    private static final String PROVIDER_ID = "1234567890";
    private static final String REFRESH_TOKEN = "generatedRefreshToken";
    private static final String ACCESS_TOKEN = "generatedAccessToken";
    private User newUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "accessTokenExpirationTime", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(userService, "refreshTokenExpirationTime", REFRESH_TOKEN_EXPIRATION);
        newUser = createTestUser(PROVIDER_ID);
    }

    @Test
    @DisplayName("유저 회원가입 테스트")
    void registerUser() {
        // Given
        UserRequest.UserRegisterDto userRegisterDto = new UserRequest.UserRegisterDto();
        userRegisterDto.setNickName("testUser");
        userRegisterDto.setStatus("대학생");

        when(jwtUtil.isRegisterTokenValid(REGISTER_TOKEN)).thenReturn(true);
        when(jwtUtil.getProviderIdFromToken(REGISTER_TOKEN)).thenReturn(PROVIDER_ID);
        when(jwtUtil.generateRefreshToken(anyLong())).thenReturn(REFRESH_TOKEN);
        when(jwtUtil.generateAccessToken(anyLong())).thenReturn(ACCESS_TOKEN);
        when(userRepository.existsByProviderId(PROVIDER_ID)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(cookieUtil.createTokenCookie(eq("refreshToken"), eq(REFRESH_TOKEN), eq(REFRESH_TOKEN_EXPIRATION)))
                .thenReturn(ResponseCookie.from("refreshToken", REFRESH_TOKEN).build());
        when(cookieUtil.createTokenCookie(eq("accessToken"), eq(ACCESS_TOKEN), eq(ACCESS_TOKEN_EXPIRATION)))
                .thenReturn(ResponseCookie.from("accessToken", ACCESS_TOKEN).build());

        // When
        UserResponse.UserDto userDto = userService.registerUser(response, REGISTER_TOKEN, userRegisterDto);

        // Then
        assertThat(userDto.getNickname()).isEqualTo(newUser.getNickName());
        assertThat(userDto.getStatus()).isEqualTo(newUser.getStatus().getValue());

        ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, times(2)).addHeader(eq("Set-Cookie"), cookieCaptor.capture());

        assertThat(cookieCaptor.getAllValues()).containsExactlyInAnyOrder(
                ResponseCookie.from("refreshToken", REFRESH_TOKEN).build().toString(),
                ResponseCookie.from("accessToken", ACCESS_TOKEN).build().toString()
        );

    }

    @Test
    @DisplayName("회원 정보 조회 테스트")
    void getUserInfo() {
        // Given
        when(userRepository.findById(newUser.getUserId())).thenReturn(Optional.of(newUser));

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

        when(userRepository.findById(newUser.getUserId())).thenReturn(Optional.of(newUser));

        // When
        userService.updateUser(newUser.getUserId(), updateDto);

        // Then
        assertThat(newUser.getNickName()).isEqualTo("editName");
        assertThat(newUser.getStatus()).isEqualTo(Status.INTERN);
        verify(userRepository).findById(newUser.getUserId());
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
                () -> userService.registerUser(response, REGISTER_TOKEN, userRegisterDto));
        assertThat(exception.getUserErrorStatus()).isEqualTo(UserErrorStatus.INVALID_USER_NICKNAME);
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
                () -> userService.registerUser(response, REGISTER_TOKEN, userRegisterDto));
        assertThat(exception.getUserErrorStatus()).isEqualTo(UserErrorStatus.INVALID_USER_NICKNAME);
    }

    private User createTestUser(String providerId) {
        return User.builder()
                .userId(1L)
                .providerId(providerId)
                .nickName("testUser")
                .status(Status.UNIVERSITY_STUDENT)
                .build();
    }
}
