package corecord.dev.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class UserResponse {

    @Data
    @Builder
    @AllArgsConstructor
    public static class UserRegisterDto {
        private Long userId;
        private String nickname;
        private String status;
        private String accessToken;
    }
}
