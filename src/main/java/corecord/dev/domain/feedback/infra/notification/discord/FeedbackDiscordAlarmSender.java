package corecord.dev.domain.feedback.infra.notification.discord;

import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.feedback.application.FeedbackAlarmSender;
import corecord.dev.domain.feedback.domain.dto.request.FeedbackRequest;
import corecord.dev.domain.feedback.infra.notification.converter.FeedbackAlarmConverter;
import corecord.dev.domain.feedback.infra.notification.dto.FeedbackAlarmDto;
import corecord.dev.domain.record.domain.entity.Record;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackDiscordAlarmSender implements FeedbackAlarmSender {
    private final Environment environment;
    private final FeedbackDiscordUtil discordUtil;
    private final WebClient webClient = WebClient.create();

    @Value("${logging.discord.feedback-web-hook-url}")
    private String feedbackWebHookUrl;

    @Override
    public void sendChatFeedbackAlarm(Record record, FeedbackRequest.FeedbackDto feedbackDto, List<Chat> chatList) {
        String content = discordUtil.createChatFeedbackContent(chatList);
        sendFeedbackDiscordAlarm(FeedbackAlarmConverter.toFeedbackAlarmDto(feedbackDto, content));
    }

    @Override
    public void sendAnalysisFeedbackAlarm(Record record, FeedbackRequest.FeedbackDto feedbackDto, Analysis analysis) {
        String content = discordUtil.createAnalysisFeedbackContent(record, analysis);
        sendFeedbackDiscordAlarm(FeedbackAlarmConverter.toFeedbackAlarmDto(feedbackDto, content));
    }

    private void sendFeedbackDiscordAlarm(FeedbackAlarmDto feedbackAlarmDto) {
        if (Arrays.asList(environment.getActiveProfiles()).contains("local")) {
            webClient.post()
                    .uri(feedbackWebHookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(discordUtil.createFeedbackMessage(feedbackAlarmDto))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        }
    }
}
