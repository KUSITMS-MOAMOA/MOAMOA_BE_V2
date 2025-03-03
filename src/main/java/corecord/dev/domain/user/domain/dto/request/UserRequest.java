package corecord.dev.domain.user.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class UserRequest {
    @Data
    public static class UserRegisterDto {
        @NotBlank(message = "닉네임을 입력해주세요.")
        private String nickName;
        @NotBlank(message = "신분 상태를 입력해주세요.")
        private String status;
    }

    @Data
    public static class UserUpdateDto {
        private String nickName;
        private String status;
    }
}
