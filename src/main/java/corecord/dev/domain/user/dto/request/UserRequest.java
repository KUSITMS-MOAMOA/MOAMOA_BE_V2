package corecord.dev.domain.user.dto.request;

import lombok.Data;

public class UserRequest {
    @Data
    public static class UserRegisterDto {
        private String nickName;
        private String status;
    }
}
