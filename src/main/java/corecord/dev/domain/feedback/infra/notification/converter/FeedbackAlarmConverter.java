package corecord.dev.domain.feedback.infra.notification.converter;

import corecord.dev.domain.feedback.domain.dto.request.FeedbackRequest;
import corecord.dev.domain.feedback.infra.notification.dto.FeedbackAlarmDto;

public class FeedbackAlarmConverter {
    public static FeedbackAlarmDto toFeedbackAlarmDto(FeedbackRequest.FeedbackDto feedback, String content) {
        return FeedbackAlarmDto.builder()
                .recordId(feedback.getRecordId())
                .feedbackType(feedback.getFeedbackType())
                .issue(feedback.getIssue())
                .comment(feedback.getComment())
                .content(content)
                .build();
    }
}

