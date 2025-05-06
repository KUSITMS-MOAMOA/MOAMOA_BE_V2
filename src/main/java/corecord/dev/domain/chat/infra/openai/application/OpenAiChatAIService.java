package corecord.dev.domain.chat.infra.openai.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.common.util.ResourceLoader;
import corecord.dev.domain.chat.application.ChatAIService;
import corecord.dev.domain.chat.domain.dto.response.ChatSummaryAiResponse;
import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.chat.exception.ChatException;
import corecord.dev.domain.chat.infra.gemini.application.GeminiChatAIService;
import corecord.dev.domain.chat.status.ChatErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Primary
@Service
@RequiredArgsConstructor
public class OpenAiChatAIService implements ChatAIService {
    private final OpenAiChatModel chatModel;
    private final GeminiChatAIService geminiChatAIService;
    private static final String CHAT_SYSTEM_CONTENT = ResourceLoader.getResourceContent("chat-prompt.txt");
    private static final String SUMMARY_SYSTEM_CONTENT = ResourceLoader.getResourceContent("chat-summary-prompt.txt");

    @Override
    public String generateChatResponse(List<Chat> chatHistory, String userContent) {
        List<Map<String, String>> messages = createRequest(CHAT_SYSTEM_CONTENT, chatHistory);
        messages.add(Map.of("role", "user", "content", userContent));

        try {
            return chatModel.call(String.valueOf(messages));
        } catch (HttpServerErrorException | WebClientException e) {
            return geminiChatAIService.generateChatResponse(chatHistory, userContent);
        }
    }

    @Override
    public ChatSummaryAiResponse generateChatSummaryResponse(List<Chat> chatHistory) {
        List<Map<String, String>> messages = createRequest(SUMMARY_SYSTEM_CONTENT, chatHistory);

        String response;
        try {
            response =  chatModel.call(String.valueOf(messages));
        } catch (HttpServerErrorException | WebClientException e) {
            response = geminiChatAIService.generateChatSummaryResponse(chatHistory).getContent();
        }

        return parseChatSummaryResponse(response);
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

    private ChatSummaryAiResponse parseChatSummaryResponse(String aiResponse) {
        String cleanedAiResponse = aiResponse.replaceAll("```json\\s*", "").replaceAll("```", "");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(cleanedAiResponse, ChatSummaryAiResponse.class);
        } catch (JsonProcessingException e) {
            throw new ChatException(ChatErrorStatus.INVALID_CHAT_SUMMARY);
        }
    }
}