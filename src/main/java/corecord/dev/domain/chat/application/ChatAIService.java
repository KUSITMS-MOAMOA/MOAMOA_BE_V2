package corecord.dev.domain.chat.application;

import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.chat.domain.dto.response.ChatSummaryAiResponse;

import java.util.List;

public interface ChatAIService {
    String generateChatResponse(List<Chat> chatHistory, String userContent);
    ChatSummaryAiResponse generateChatSummaryResponse(List<Chat> chatHistory);
}
