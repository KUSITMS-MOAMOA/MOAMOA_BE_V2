package corecord.dev.domain.chat.dto.response;

import corecord.dev.domain.chat.dto.request.ChatRequest;
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

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class ChatSummaryDto {
        private Long chatRoomId;
        private String title;
        private String content;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class ChatTmpDto {
        private Long chatRoomId;
        private boolean isExist;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class ChatsDto {
        private List<ChatDto> chats;
    }

}
