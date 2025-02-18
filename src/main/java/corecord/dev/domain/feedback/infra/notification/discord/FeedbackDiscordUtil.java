package corecord.dev.domain.feedback.infra.notification.discord;

import corecord.dev.common.log.discord.dto.DiscordDto;
import corecord.dev.domain.ability.domain.entity.Ability;
import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.feedback.infra.notification.dto.FeedbackAlarmDto;
import corecord.dev.domain.record.domain.entity.Record;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FeedbackDiscordUtil {
    public DiscordDto.MessageDto createFeedbackMessage(FeedbackAlarmDto feedbackAlarmDto) {
        return DiscordDto.MessageDto.builder()
                .content("# 🚨 AI 피드백 불만족 알림 🚨")
                .embeds(List.of(
                        DiscordDto.EmbedDto.builder()
                                .title("📌 피드백 정보")
                                .description(
                                        "### 🕒 피드백 작성 시간\n" +
                                                ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                                                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH시 mm분 ss초")) + "\n\n" +
                                                "### 🔍 관련 기록 (Record ID)\n" +
                                                feedbackAlarmDto.getRecordId() + "\n\n" +
                                                "### 📄 피드백 종류\n" +
                                                feedbackAlarmDto.getFeedbackType() + "\n\n" +
                                                (feedbackAlarmDto.getIssue() != null ?
                                                        "### ❗ 피드백 제목\n" + feedbackAlarmDto.getIssue().getDescription() + "\n\n" : "") +
                                                (feedbackAlarmDto.getComment() != null ?
                                                        "### 💬 사용자 직접 입력 코멘트\n" +
                                                                feedbackAlarmDto.getComment() + "\n\n" : "") +
                                                "### 📜 관련 내용\n" +
                                                "```\n" + feedbackAlarmDto.getContent() + "\n```"
                                )
                                .build()
                )).build();
    }

    public String createChatFeedbackContent(List<Chat> chatList) {
        return chatList.stream()
                .map(chat -> String.format("[%s] : %s", chat.getAuthor() == 0 ? "AI" : "사용자", chat.getContent()))
                .collect(Collectors.joining("\n"));
    }

    public String createAnalysisFeedbackContent(Record record, Analysis analysis) {
        return String.format("[경험 기록]\n%s\n\n[핵심 역량]\n%s\n[보완점]\n%s\n",
                record.getContent(),
                analysis.getAbilityList().stream()
                        .map(ability -> String.format("- %s: %s", ability.getKeyword().getValue(), ability.getContent()))
                        .collect(Collectors.joining("\n")),
                analysis.getComment());
    }
}
