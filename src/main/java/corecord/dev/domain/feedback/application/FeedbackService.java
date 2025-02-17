package corecord.dev.domain.feedback.application;

import corecord.dev.domain.feedback.domain.dto.request.FeedbackRequest;

public interface FeedbackService {
    void saveFeedback(Long userId, FeedbackRequest.FeedbackDto feedbackDto);
}
