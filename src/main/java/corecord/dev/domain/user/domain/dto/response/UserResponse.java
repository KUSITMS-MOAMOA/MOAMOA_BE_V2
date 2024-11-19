package corecord.dev.domain.user.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class UserResponse {

    @Data
    @Builder
    @AllArgsConstructor
    public static class UserDto {
        private Long userId;
        private String nickname;
        private String status;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class UserInfoDto {
        private int recordCount;
        private String nickname;
        private String status;
    }
}
