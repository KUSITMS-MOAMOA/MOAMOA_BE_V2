package corecord.dev.domain.user.converter;

import corecord.dev.domain.user.dto.request.UserRequest;
import corecord.dev.domain.user.dto.response.UserResponse;
import corecord.dev.domain.user.entity.Status;
import corecord.dev.domain.user.entity.User;

public class UserConverter {

    public static User toUserEntity(UserRequest.UserRegisterDto request, String providerId) {
        return User.builder()
                .providerId(providerId)
                .nickName(request.getNickName())
                .status(Status.getStatus(request.getStatus()))
                .build();
    }

    public static UserResponse.UserDto toUserDto(User user) {
        return UserResponse.UserDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickName())
                .status(user.getStatus().getValue())
                .build();
    }

    public static UserResponse.UserInfoDto toUserInfoDto(User user, int recordCount) {
        return UserResponse.UserInfoDto.builder()
                .recordCount(recordCount)
                .nickname(user.getNickName())
                .status(user.getStatus().getValue())
                .build();
    }

}
