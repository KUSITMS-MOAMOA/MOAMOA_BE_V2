package corecord.dev.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

public class ChatResponse {

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class ChatRoomDto {
        private Long chatRoomId;
        private String firstChat;
    }
}
