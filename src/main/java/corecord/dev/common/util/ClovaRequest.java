package corecord.dev.common.util;

import corecord.dev.domain.chat.entity.Chat;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class ClovaRequest {
    private static final int CHAT_MAX_TOKENS = 256;
    private static final int SUMMARY_MAX_TOKENS = 500;
    private static final int ABILITY_ANALYSIS_MAX_TOKENS = 500;
    private static final String CHAT_SYSTEM_CONTENT = ResourceLoader.getResourceContent("chat-prompt.txt");
    private static final String SUMMARY_SYSTEM_CONTENT = ResourceLoader.getResourceContent("chat-summary-prompt.txt");
    private static final String ABILITY_ANALYSIS_SYSTEM_CONTENT = ResourceLoader.getResourceContent("ability-analysis-prompt.txt");

    private List<Map<String, String>> messages;
    private final double topP = 0.8;
    private final int topK = 0;
    private final int maxTokens;
    private final double temperature = 0.5;
    private final double repeatPenalty = 5.0;
    private final boolean includeAiFilters = true;
    private final int seed = 0;

    public ClovaRequest(List<Map<String, String>> messages, int max_tokens) {
        this.messages = messages;
        this.maxTokens = max_tokens;
    }

    public static ClovaRequest createChatRequest(List<Chat> chatHistory, String userContent) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 시스템 메시지 추가
        messages.add(Map.of(
                "role", "system",
                "content", CHAT_SYSTEM_CONTENT
        ));

        // 기존 채팅 내역 추가
        for (Chat chat : chatHistory) {
            String role = chat.getAuthor() == 0 ? "assistant" : "user";
            messages.add(Map.of("role", role, "content", chat.getContent()));
        }

        // 사용자 입력 추가
        messages.add(Map.of("role", "user", "content", userContent));

        return new ClovaRequest(messages, CHAT_MAX_TOKENS);
    }

    public static ClovaRequest createChatSummaryRequest(List<Chat> chatHistory) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 시스템 메시지 추가
        messages.add(Map.of(
                "role", "system",
                "content", SUMMARY_SYSTEM_CONTENT
        ));

        // 기존 채팅 내역을 하나의 문자열로 병합
        StringBuilder chatContentBuilder = new StringBuilder();
        for (Chat chat : chatHistory) {
            String role = chat.getAuthor() == 0 ? "ai" : "recorder";
            chatContentBuilder.append(role).append(": ").append(chat.getContent()).append("\n");
        }
        String chatContent = chatContentBuilder.toString();

        // 병합된 내용을 추가
        messages.add(Map.of(
                "role", "user",
                "content", chatContent
        ));

        return new ClovaRequest(messages, SUMMARY_MAX_TOKENS);
    }

    public static ClovaRequest createAnalysisRequest(String content) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 시스템 메세지 추가
        messages.add(Map.of(
                "role", "system",
                "content", ABILITY_ANALYSIS_SYSTEM_CONTENT
        ));

        messages.add(Map.of(
                "role", "user",
                "content", content
        ));
        return new ClovaRequest(messages, ABILITY_ANALYSIS_MAX_TOKENS);
    }

}
