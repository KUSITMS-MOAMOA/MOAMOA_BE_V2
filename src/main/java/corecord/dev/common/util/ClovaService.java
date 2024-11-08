package corecord.dev.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClovaService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;

    public Mono<String> generateAiResponse(ClovaRequest clovaRequest) {
        log.info("AI 요청: {}", clovaRequest.getMessages());

        return webClient.post()
                .bodyValue(clovaRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                .bodyToMono(String.class)
                .flatMap(this::parseContentFromResponse)
                .doOnError(e -> log.error("채팅 AI 응답 생성 실패", e));
    }

    private Mono<String> parseContentFromResponse(String responseBody) {
        return Mono.fromCallable(() -> {
                    JsonNode root = objectMapper.readTree(responseBody);
                    return root.path("result").path("message").path("content").asText();
                }).doOnError(e -> log.error("응답 파싱 실패", e))
                .onErrorMap(e -> new GeneralException(ErrorStatus.AI_RESPONSE_ERROR));
    }

    private Mono<Throwable> handleClientError(ClientResponse clientResponse) {
        log.error("클라이언트 오류 발생: 상태 코드 - {}", clientResponse.statusCode());
        return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(new GeneralException(ErrorStatus.AI_CLIENT_ERROR)));
    }

    private Mono<Throwable> handleServerError(ClientResponse clientResponse) {
        log.error("서버 오류 발생: 상태 코드 - {}", clientResponse.statusCode());
        return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(new GeneralException(ErrorStatus.AI_SERVER_ERROR)));
    }
}
