package corecord.dev.domain.feedback.presentation;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.web.UserId;
import corecord.dev.domain.feedback.application.FeedbackService;
import corecord.dev.domain.feedback.domain.dto.request.FeedbackRequest;
import corecord.dev.domain.feedback.domain.status.FeedbackSuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feedback")
public class FeedbackController {
    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> saveFeedback(
            @UserId Long userId,
            @RequestBody @Valid FeedbackRequest.FeedbackDto feedbackDto
    ) {
        feedbackService.saveFeedback(userId, feedbackDto);
        return ApiResponse.success(FeedbackSuccessStatus.FEEDBACK_POST_SUCCESS);

    }
}
