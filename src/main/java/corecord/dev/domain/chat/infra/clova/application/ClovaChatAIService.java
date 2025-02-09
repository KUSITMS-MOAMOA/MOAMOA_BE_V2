package corecord.dev.domain.chat.infra.clova.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.common.util.ClovaUtil;
import corecord.dev.domain.chat.application.ChatAIService;
import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.chat.exception.ChatException;
import corecord.dev.domain.chat.infra.clova.dto.request.ClovaRequest;
import corecord.dev.domain.chat.domain.dto.response.ChatSummaryAiResponse;
import corecord.dev.domain.chat.status.ChatErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClovaChatAIService implements ChatAIService {

    private final ClovaUtil clovaUtil;

    @Override
    public String generateChatResponse(List<Chat> chatHistory, String userInput) {
        try {
            ClovaRequest clovaRequest = ClovaRequest.createChatRequest(chatHistory, userInput);
            String responseBody = clovaUtil.postWebClient(clovaRequest);

            return clovaUtil.parseContentFromResponse(responseBody);
        } catch (WebClientException e) {
            log.error("채팅 AI 응답 생성 실패", e);
            throw new ChatException(ChatErrorStatus.AI_RESPONSE_ERROR);
        }
    }

    @Override
    public ChatSummaryAiResponse generateChatSummaryResponse(List<Chat> chatHistory) {
        try {
            ClovaRequest clovaRequest = ClovaRequest.createChatSummaryRequest(chatHistory);
            String responseBody = clovaUtil.postWebClient(clovaRequest);
            String aiResponse = clovaUtil.parseContentFromResponse(responseBody);

            return parseChatSummaryResponse(aiResponse);
        } catch (WebClientException e) {
            log.error("채팅 AI 응답 생성 실패", e);
            throw new ChatException(ChatErrorStatus.AI_RESPONSE_ERROR);
        }
    }

    private ChatSummaryAiResponse parseChatSummaryResponse(String aiResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(aiResponse, ChatSummaryAiResponse.class);
        } catch (JsonProcessingException e) {
            throw new ChatException(ChatErrorStatus.INVALID_CHAT_SUMMARY);
        }
    }
}
