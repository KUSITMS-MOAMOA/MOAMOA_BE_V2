package corecord.dev.domain.feedback.application;

import corecord.dev.domain.feedback.domain.converter.FeedbackConverter;
import corecord.dev.domain.feedback.domain.dto.request.FeedbackRequest;
import corecord.dev.domain.feedback.domain.entity.Feedback;
import corecord.dev.domain.feedback.domain.entity.Satisfaction;
import corecord.dev.domain.feedback.domain.exception.FeedbackException;
import corecord.dev.domain.feedback.domain.status.FeedbackErrorStatus;
import corecord.dev.domain.record.application.RecordDbService;
import corecord.dev.domain.record.domain.entity.Record;
import corecord.dev.domain.user.application.UserDbService;
import corecord.dev.domain.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
    private final UserDbService userDbService;
    private final RecordDbService recordDbService;
    private final FeedbackDbService feedbackDbService;

    /**
     * 피드백 저장
     *
     * @param userId
     * @param feedbackDto
     */
    @Override
    public void saveFeedback(Long userId, FeedbackRequest.FeedbackDto feedbackDto) {
        User user = userDbService.findUserById(userId);

        // 해당 user의 Record 인지 확인
        Record record = getValidatedRecord(user, feedbackDto.getRecordId());

        // 이미 피드백을 작성한 경험인지 확인
        checkAlreadyFeedback(user, record.getRecordId());

        if (feedbackDto.getSatisfaction() == Satisfaction.BAD) {
            // 피드백 content 길이 확인
            checkCommentLength(feedbackDto.getComment());
        }

        Feedback feedback = FeedbackConverter.toFeedbackEntity(user, feedbackDto);
        feedbackDbService.saveFeedback(feedback);
    }

    private Record getValidatedRecord(User user, Long recordId) {
        Record record = recordDbService.findRecordById(recordId);
        if (!record.getUser().getUserId().equals(user.getUserId())) {
            throw new FeedbackException(FeedbackErrorStatus.UNAUTHORIZED_FEEDBACK);
        }
        return record;
    }

    private void checkAlreadyFeedback(User user, Long recordId) {
        if (feedbackDbService.existsByUserAndRecordId(user, recordId)) {
            throw new FeedbackException(FeedbackErrorStatus.ALREADY_FEEDBACK);
        }
    }

    private void checkCommentLength(String comment) {
        if (comment != null && comment.length() > 200) {
            throw new FeedbackException(FeedbackErrorStatus.OVERFLOW_FEEDBACK_COMMENT);
        }
    }
}
