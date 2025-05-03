package corecord.dev.domain.user.domain.converter;

import corecord.dev.domain.user.domain.dto.request.UserRequest;
import corecord.dev.domain.user.domain.dto.response.UserResponse;
import corecord.dev.domain.user.domain.enums.Provider;
import corecord.dev.domain.user.domain.enums.Status;
import corecord.dev.domain.user.domain.entity.User;

public class UserConverter {

    public static User toUserEntity(UserRequest.UserRegisterDto request, String providerId, Provider provider) {
        return User.builder()
                .providerId(providerId)
                .provider(provider)
                .nickName(request.getNickName())
                .status(Status.getStatus(request.getStatus()))
                .build();
    }

    public static UserResponse.UserDto toUserDto(User user, String accessToken, String refreshToken) {
        return UserResponse.UserDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickName())
                .status(user.getStatus().getValue())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
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
