package corecord.dev.domain.chat.infra.gemini.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.common.util.GeminiUtil;
import corecord.dev.domain.chat.application.ChatAIService;
import corecord.dev.domain.chat.domain.dto.response.ChatSummaryAiResponse;
import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.chat.exception.ChatException;
import corecord.dev.domain.chat.infra.clova.application.ClovaChatAIService;
import corecord.dev.domain.chat.infra.gemini.dto.request.GeminiChatRequest;
import corecord.dev.domain.chat.status.ChatErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiChatAIService implements ChatAIService {

    private final GeminiUtil geminiUtil;
    private final ClovaChatAIService clovaChatAIService;

    @Override
    public String generateChatResponse(List<Chat> chatHistory, String userContent) {
        try {
            GeminiChatRequest geminiRequest = GeminiChatRequest.createChatRequest(chatHistory, userContent);
            String responseBody = geminiUtil.postWebClient(geminiRequest);
            return geminiUtil.parseContentFromResponse(responseBody);
        } catch (HttpServerErrorException | WebClientException e) {
            return clovaChatAIService.generateChatResponse(chatHistory, userContent);
        }
    }

    @Override
    public ChatSummaryAiResponse generateChatSummaryResponse(List<Chat> chatHistory) {
        try {
            GeminiChatRequest geminiRequest = GeminiChatRequest.createChatSummaryRequest(chatHistory);
            String responseBody = geminiUtil.postWebClient(geminiRequest);
            String aiResponse = geminiUtil.parseContentFromResponse(responseBody);
            return parseChatSummaryResponse(aiResponse);
        } catch (HttpServerErrorException | WebClientException e) {
            return clovaChatAIService.generateChatSummaryResponse(chatHistory);
        }
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
