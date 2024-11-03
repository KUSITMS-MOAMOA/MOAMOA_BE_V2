package corecord.dev.domain.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.domain.chat.dto.request.ChatSummaryRequest;
import corecord.dev.domain.chat.exception.enums.ChatErrorStatus;
import corecord.dev.domain.chat.exception.model.ChatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

@Service
@Slf4j
@RequiredArgsConstructor
public class SummaryService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient = WebClient.create();

    @Value("${ncp.chat-ai.host}")
    private String chatAiHost;

    @Value("${ncp.chat-ai.api-key}")
    private String chatAiApiKey;

    @Value("${ncp.chat-ai.api-key-primary-val}")
    private String chatAiApiKeyPrimaryVal;

    @Value("${ncp.chat-ai.request-id}")
    private String chatAiRequestId;

    public String generateAiResponse(ChatSummaryRequest chatSummaryRequest) {
        try {
            log.info("AI 요청: {}", chatSummaryRequest.getText());

            String responseBody = webClient.post()
                    .uri(chatAiHost)
                    .header("X-NCP-CLOVASTUDIO-API-KEY", chatAiApiKey)
                    .header("X-NCP-APIGW-API-KEY", chatAiApiKeyPrimaryVal)
                    .header("X-NCP-CLOVASTUDIO-REQUEST-ID", chatAiRequestId)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .bodyValue(chatSummaryRequest)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        log.error("클라이언트 오류 발생: 상태 코드 - {}", clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                                .map(errorBody -> new ChatException(ChatErrorStatus.CHAT_CLIENT_ERROR));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                        log.error("서버 오류 발생: 상태 코드 - {}", clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                                .map(errorBody -> new ChatException(ChatErrorStatus.CHAT_SERVER_ERROR));
                    })
                    .bodyToMono(String.class)
                    .block();

            log.info("AI 응답: {}", responseBody);
            return parseContentFromResponse(responseBody);
        } catch (WebClientException e) {
            log.error("채팅 AI 응답 생성 실패", e);
            throw new ChatException(ChatErrorStatus.CHAT_AI_RESPONSE_ERROR);
        }
    }

    private String parseContentFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode resultText = root.path("result").path("text");
            return resultText.asText();
        } catch (Exception e) {
            log.error("응답 파싱 실패", e);
            throw new ChatException(ChatErrorStatus.CHAT_AI_RESPONSE_ERROR);
        }
    }
}
