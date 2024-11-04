package corecord.dev.domain.chat.dto.request;


import corecord.dev.domain.chat.entity.Chat;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class ChatSummaryRequest {
    private final String text;
    private final String start = "";
    private final String restart = "";
    private final boolean includeTokens = false;
    private final double topP = 0.8;
    private final int topK = 4;
    private final int maxTokens = 500;
    private final double temperature = 0.85;
    private final double repeatPenalty = 5.0;
    private final boolean includeAiFilters = true;
    private final boolean includeProbs = false;
    private final List<String> stopBefore = Collections.singletonList("<|endoftext|>");

    public ChatSummaryRequest(String text) {
        this.text = text;
    }

    public static ChatSummaryRequest createChatSummaryRequest(List<Chat> chatHistory) {

        // 기존 채팅 내역을 하나의 문자열로 병합
        StringBuilder chatContentBuilder = new StringBuilder();
        for (Chat chat : chatHistory) {
            String role = chat.getAuthor() == 0 ? "Ai" : "User";
            chatContentBuilder.append(role).append(": ").append(chat.getContent()).append("\n");
        }
        String chatContent = chatContentBuilder.toString();

        return new ChatSummaryRequest(chatContent);
    }
}
