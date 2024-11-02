package corecord.dev.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

public class ChatResponse {

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class ChatRoomDto {
        private Long chatRoomId;
        private String firstChat;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class ChatDto {
        private Long chatId;
        private String content;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class ChatListDto {
        private List<ChatDetailDto> chats;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class ChatDetailDto {
        private Long chatId;
        private String author;
        private String content;
        private String created_at;
    }


}
