package corecord.dev.domain.feedback.domain.converter;

import corecord.dev.domain.feedback.domain.dto.request.FeedbackRequest;
import corecord.dev.domain.feedback.domain.entity.Feedback;
import corecord.dev.domain.user.domain.entity.User;

public class FeedbackConverter {
    public static Feedback toFeedbackEntity(User user, FeedbackRequest.FeedbackDto feedbackDto) {
        return Feedback.builder()
                .satisfaction(feedbackDto.getSatisfaction())
                .feedbackType(feedbackDto.getFeedbackType())
                .issue(feedbackDto.getIssue())
                .comment(feedbackDto.getComment())
                .recordId(feedbackDto.getRecordId())
                .user(user)
                .build();
    }
}
