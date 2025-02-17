package corecord.dev.domain.user.application;

import corecord.dev.domain.user.domain.dto.request.UserRequest;
import corecord.dev.domain.user.domain.dto.response.UserResponse;

public interface UserService {

    UserResponse.UserDto registerUser(String registerToken, UserRequest.UserRegisterDto userRegisterDto);
    void logoutUser(String refreshToken);
    void deleteUser(Long userId, String refreshToken);
    void updateUser(Long userId, UserRequest.UserUpdateDto userUpdateDto);
    UserResponse.UserInfoDto getUserInfo(Long userId);

}
