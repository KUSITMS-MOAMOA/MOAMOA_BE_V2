package corecord.dev.domain.chat.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

public class ChatRequest {

    @Getter
    @Builder
    public static class ChatDto {
        private boolean guide;
        @NotBlank(message = "채팅을 입력해주세요.")
        private String content;
    }
}
