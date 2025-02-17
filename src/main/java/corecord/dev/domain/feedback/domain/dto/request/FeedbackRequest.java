package corecord.dev.domain.feedback.domain.dto.request;

import corecord.dev.domain.feedback.domain.entity.FeedbackType;
import corecord.dev.domain.feedback.domain.entity.Issue;
import corecord.dev.domain.feedback.domain.entity.Satisfaction;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

public class FeedbackRequest {

    @Data
    @Builder
    public static class FeedbackDto {
        @NotNull(message = "recordId를 입력해주세요.")
        private Long recordId;
        @NotNull(message = "만족 여부를 입력해주세요.")
        private Satisfaction satisfaction;
        private FeedbackType feedbackType;
        private Issue issue;
        private String comment;
    }
}
