package corecord.dev.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.domain.chat.exception.ChatException;
import corecord.dev.domain.chat.status.ChatErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class GeminiUtil {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient = WebClient.create();

    @Value("${spring.ai.gemini.host}")
    private String aiHost;

    @Value("${spring.ai.gemini.api-key}")
    private String apiKey;

    public String postWebClient(Object geminiRequest) {
        return webClient.post()
                .uri(aiHost + "?key=" + apiKey)
                .header("Accept", "application/json")
                .bodyValue(geminiRequest)
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

    public String parseContentFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode messageContent = root.path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text");
            return messageContent.asText();
        } catch (Exception e) {
            log.error("응답 파싱 실패", e);
            throw new ChatException(ChatErrorStatus.INVALID_CHAT_RESPONSE);
        }
    }
}
