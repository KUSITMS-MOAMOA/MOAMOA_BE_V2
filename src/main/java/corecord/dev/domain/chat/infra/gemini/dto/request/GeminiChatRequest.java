package corecord.dev.domain.chat.infra.gemini.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import corecord.dev.common.util.ResourceLoader;
import corecord.dev.domain.chat.domain.entity.Chat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
public class GeminiChatRequest {
    private static final String CHAT_SYSTEM_CONTENT = ResourceLoader.getResourceContent("chat-prompt.txt");
    private static final String SUMMARY_SYSTEM_CONTENT = ResourceLoader.getResourceContent("chat-summary-prompt.txt");

    private List<Content> contents;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Content{
        private List<Part> parts;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Part{
        private String text;
    }

    public GeminiChatRequest(String content) {
        this.contents = List.of(new GeminiChatRequest.Content(List.of(new GeminiChatRequest.Part(content))));
    }

    public static GeminiChatRequest createChatRequest(List<Chat> chatHistory, String content) {
        List<Map<String, String>> messages = createRequest(CHAT_SYSTEM_CONTENT, chatHistory);
        messages.add(Map.of("role", "user", "content", content));

        return new GeminiChatRequest(String.valueOf(messages));
    }

    public static GeminiChatRequest createChatSummaryRequest(List<Chat> chatHistory) {
        return new GeminiChatRequest(String.valueOf(createRequest(SUMMARY_SYSTEM_CONTENT, chatHistory)));
    }

    private static List<Map<String, String>> createRequest(String systemContent, List<Chat> chatHistory) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 시스템 메시지 추가
        messages.add(Map.of(
                "role", "system",
                "content", systemContent
        ));

        // 기존 채팅 내역 추가
        for (Chat chat : chatHistory) {
            String role = chat.getAuthor() == 0 ? "assistant" : "user";
            messages.add(Map.of("role", role, "content", chat.getContent()));
        }

        return messages;
    }
}
