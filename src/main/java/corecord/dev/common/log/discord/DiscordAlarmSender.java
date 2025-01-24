package corecord.dev.common.log.discord;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordAlarmSender {

    private final Environment environment;
    @Value("${logging.discord.web-hook-url}")
    private String webHookUrl;

    private final DiscordUtil discordUtil;
    private final WebClient webClient = WebClient.create();

    public Void sendDiscordAlarm(Exception exception, HttpServletRequest httpServletRequest) {
        if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            return webClient.post()
                    .uri(webHookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(discordUtil.createMessage(exception, httpServletRequest))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        }
        return null;
    }
}
