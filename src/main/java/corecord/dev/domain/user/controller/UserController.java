package corecord.dev.domain.user.controller;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.status.SuccessStatus;
import corecord.dev.common.web.UserId;
import corecord.dev.domain.user.constant.UserSuccessStatus;
import corecord.dev.domain.user.dto.request.UserRequest;
import corecord.dev.domain.user.dto.response.UserResponse;
import corecord.dev.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> getSuccess(
            @UserId Long userId
    ) {
        return ApiResponse.success(SuccessStatus.OK, "userId: " + userId);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse.UserRegisterDto>> registerUser(
            HttpServletResponse response,
            @RequestHeader("registerToken") String registerToken,
            @RequestBody UserRequest.UserRegisterDto userRegisterDto
            ) {
        UserResponse.UserRegisterDto registerResponse = userService.registerUser(response, registerToken, userRegisterDto);
        return ApiResponse.success(UserSuccessStatus.USER_REGISTER_SUCCESS, registerResponse);
    }

    @GetMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logoutUser(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        userService.logoutUser(request, response);
        return ApiResponse.success(UserSuccessStatus.USER_LOGOUT_SUCCESS);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> deleteUser(
            HttpServletRequest request,
            HttpServletResponse response,
            @UserId Long userId
    ) {
        userService.deleteUser(request, response, userId);
        return ApiResponse.success(UserSuccessStatus.USER_DELETE_SUCCESS);
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<String>> updateUser(
            @UserId Long userId,
            @RequestBody UserRequest.UserUpdateDto userUpdateDto
    ) {
        userService.updateUser(userId, userUpdateDto);
        return ApiResponse.success(UserSuccessStatus.USER_UPDATE_SUCCESS);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse.UserInfoDto>> getUserInfo(
            @UserId Long userId
    ) {
        UserResponse.UserInfoDto userInfoDto = userService.getUserInfo(userId);
        return ApiResponse.success(UserSuccessStatus.GET_USER_INFO_SUCCESS, userInfoDto);
    }
}
