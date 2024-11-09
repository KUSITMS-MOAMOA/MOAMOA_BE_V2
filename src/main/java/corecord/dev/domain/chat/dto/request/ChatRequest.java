package corecord.dev.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class ChatRequest {

    @Getter
    public static class ChatDto {
        @NotBlank(message = "채팅을 입력해주세요.")
        private String content;
    }
}
