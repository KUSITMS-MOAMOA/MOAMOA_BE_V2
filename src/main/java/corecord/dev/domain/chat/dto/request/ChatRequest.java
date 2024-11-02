package corecord.dev.domain.chat.dto.request;

import lombok.Getter;

public class ChatRequest {

    @Getter
    public static class ChatDto {
        private String content;
    }
}
