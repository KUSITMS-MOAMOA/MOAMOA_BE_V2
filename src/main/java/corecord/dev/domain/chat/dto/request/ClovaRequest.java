package corecord.dev.domain.chat.dto.request;

import corecord.dev.common.util.ResourceLoader;
import corecord.dev.domain.chat.entity.Chat;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class ClovaRequest {
    private static final double TOP_P = 0.8;
    private static final int TOP_K = 0;
    private static final int MAX_TOKENS = 256;
    private static final double TEMPERATURE = 0.5;
    private static final double REPEAT_PENALTY = 5.0;
    private static final boolean INCLUDE_AI_FILTERS = true;
    private static final int SEED = 0;
    private static final String SYSTEM_CONTENT = ResourceLoader.getResourceContent("chat-prompt.txt");

    private List<Map<String, String>> messages;
    private double topP;
    private int topK;
    private int maxTokens;
    private double temperature;
    private double repeatPenalty;
    private boolean includeAiFilters;
    private int seed;

    public ClovaRequest(List<Map<String, String>> messages) {
        this.messages = messages;
        this.topP = TOP_P;
        this.topK = TOP_K;
        this.maxTokens = MAX_TOKENS;
        this.temperature = TEMPERATURE;
        this.repeatPenalty = REPEAT_PENALTY;
        this.includeAiFilters = INCLUDE_AI_FILTERS;
        this.seed = SEED;
    }

    public static ClovaRequest createRequest(List<Chat> chatHistory, String userContent) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 시스템 메시지 추가
        messages.add(Map.of(
                "role", "system",
                "content", SYSTEM_CONTENT
        ));

        // 기존 채팅 내역 추가
        for (Chat chat : chatHistory) {
            String role = chat.getAuthor() == 0 ? "assistant" : "user";
            messages.add(Map.of("role", role, "content", chat.getContent()));
        }

        // 사용자 입력 추가
        messages.add(Map.of("role", "user", "content", userContent));

        return new ClovaRequest(messages);
    }
}
