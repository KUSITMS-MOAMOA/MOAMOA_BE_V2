package corecord.dev.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${ncp.chat.host}")
    private String chatHost;

    @Value("${ncp.chat.api-key}")
    private String chatApiKey;

    @Value("${ncp.chat.api-key-primary-val}")
    private String chatApiKeyPrimaryVal;

    @Value("${ncp.chat.request-id}")
    private String chatRequestId;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl(chatHost)
                .defaultHeader("X-NCP-CLOVASTUDIO-API-KEY", chatApiKey)
                .defaultHeader("X-NCP-APIGW-API-KEY", chatApiKeyPrimaryVal)
                .defaultHeader("X-NCP-CLOVASTUDIO-REQUEST-ID", chatRequestId)
                .defaultHeader("Content-Type", "application/json; charset=utf-8")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}

