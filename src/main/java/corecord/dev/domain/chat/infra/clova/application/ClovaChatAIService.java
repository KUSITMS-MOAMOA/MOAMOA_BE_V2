package corecord.dev.domain.chat.infra.clova.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.domain.chat.application.ChatAIService;
import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.chat.exception.ChatException;
import corecord.dev.domain.chat.infra.clova.dto.request.ClovaRequest;
import corecord.dev.domain.chat.domain.dto.response.ChatSummaryAiResponse;
import corecord.dev.domain.chat.status.ChatErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClovaChatAIService implements ChatAIService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient = WebClient.create();

    @Value("${ncp.chat.host}")
    private String chatHost;

    @Value("${ncp.chat.api-key}")
    private String chatApiKey;

    @Value("${ncp.chat.api-key-primary-val}")
    private String chatApiKeyPrimaryVal;

    @Value("${ncp.chat.request-id}")
    private String chatRequestId;

    @Override
    public String generateChatResponse(List<Chat> chatHistory, String userInput) {
        try {
            ClovaRequest clovaRequest = ClovaRequest.createChatRequest(chatHistory, userInput);
            String responseBody = postWebClient(clovaRequest);

            return parseContentFromResponse(responseBody);
        } catch (WebClientException e) {
            log.error("채팅 AI 응답 생성 실패", e);
            throw new ChatException(ChatErrorStatus.AI_RESPONSE_ERROR);
        }
    }

    @Override
    public ChatSummaryAiResponse generateChatSummaryResponse(List<Chat> chatHistory) {
        try {
            ClovaRequest clovaRequest = ClovaRequest.createChatSummaryRequest(chatHistory);
            String responseBody = postWebClient(clovaRequest);
            String aiResponse = parseContentFromResponse(responseBody);

            return parseChatSummaryResponse(aiResponse);
        } catch (WebClientException e) {
            log.error("채팅 AI 응답 생성 실패", e);
            throw new ChatException(ChatErrorStatus.AI_RESPONSE_ERROR);
        }
    }

    private String postWebClient(ClovaRequest clovaRequest) {
        return webClient.post()
                .uri(chatHost)
                .header("X-NCP-CLOVASTUDIO-API-KEY", chatApiKey)
                .header("X-NCP-APIGW-API-KEY", chatApiKeyPrimaryVal)
                .header("X-NCP-CLOVASTUDIO-REQUEST-ID", chatRequestId)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "application/json")
                .bodyValue(clovaRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    log.error("클라이언트 오류 발생: 상태 코드 - {}", clientResponse.statusCode());
                    return clientResponse.bodyToMono(String.class)
                            .map(errorBody -> new ChatException(ChatErrorStatus.AI_CLIENT_ERROR));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                    log.error("서버 오류 발생: 상태 코드 - {}", clientResponse.statusCode());
                    return clientResponse.bodyToMono(String.class)
                            .map(errorBody -> new ChatException(ChatErrorStatus.AI_SERVER_ERROR));
                })
                .bodyToMono(String.class)
                .block();
    }

    private String parseContentFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode messageContent = root.path("result").path("message").path("content");
            return messageContent.asText();
        } catch (Exception e) {
            log.error("응답 파싱 실패", e);
            throw new ChatException(ChatErrorStatus.INVALID_CHAT_RESPONSE);
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
