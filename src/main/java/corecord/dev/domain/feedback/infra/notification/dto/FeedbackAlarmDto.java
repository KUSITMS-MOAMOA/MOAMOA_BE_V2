package corecord.dev.domain.feedback.infra.notification.dto;

import corecord.dev.domain.feedback.domain.entity.FeedbackType;
import corecord.dev.domain.feedback.domain.entity.Issue;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackAlarmDto {
    private Long recordId;
    private FeedbackType feedbackType;
    private Issue issue;
    private String comment;
    private String content;
}