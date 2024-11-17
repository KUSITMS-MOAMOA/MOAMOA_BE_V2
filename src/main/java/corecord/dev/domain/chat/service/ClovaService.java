package corecord.dev.domain.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
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
public class ClovaService {

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

    public String generateAiResponse(ClovaRequest clovaRequest) {
        try {
            String responseBody = webClient.post()
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
                                .map(errorBody -> new GeneralException(ErrorStatus.AI_CLIENT_ERROR));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                        log.error("서버 오류 발생: 상태 코드 - {}", clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                                .map(errorBody -> new GeneralException(ErrorStatus.AI_SERVER_ERROR));
                    })
                    .bodyToMono(String.class)
                    .block();

            return parseContentFromResponse(responseBody);
        } catch (WebClientException e) {
            log.error("채팅 AI 응답 생성 실패", e);
            throw new GeneralException(ErrorStatus.AI_RESPONSE_ERROR);
        }
    }

    private String parseContentFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode messageContent = root.path("result").path("message").path("content");
            return messageContent.asText();
        } catch (Exception e) {
            log.error("응답 파싱 실패", e);
            throw new GeneralException(ErrorStatus.AI_RESPONSE_ERROR);
        }
    }
}
