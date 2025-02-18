package corecord.dev.domain.feedback.application;

import corecord.dev.domain.analysis.application.AnalysisDbService;
import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.chat.application.ChatDbService;
import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.feedback.domain.converter.FeedbackConverter;
import corecord.dev.domain.feedback.domain.dto.request.FeedbackRequest;
import corecord.dev.domain.feedback.domain.entity.Feedback;
import corecord.dev.domain.feedback.domain.entity.FeedbackType;
import corecord.dev.domain.feedback.domain.entity.Satisfaction;
import corecord.dev.domain.feedback.domain.exception.FeedbackException;
import corecord.dev.domain.feedback.domain.status.FeedbackErrorStatus;
import corecord.dev.domain.record.application.RecordDbService;
import corecord.dev.domain.record.domain.entity.Record;
import corecord.dev.domain.user.application.UserDbService;
import corecord.dev.domain.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
    private final UserDbService userDbService;
    private final RecordDbService recordDbService;
    private final FeedbackDbService feedbackDbService;
    private final ChatDbService chatDbService;
    private final AnalysisDbService analysisDbService;
    private final FeedbackAlarmSender feedbackAlarmSender;

    /**
     * 피드백 저장
     *
     * @param userId
     * @param feedbackDto
     */

    public void saveFeedback(Long userId, FeedbackRequest.FeedbackDto feedbackDto) {
        User user = userDbService.findUserById(userId);

        // 해당 user의 Record 확인 & 피드백 중복 검사
        Record record = getValidatedRecord(user, feedbackDto.getRecordId());
        checkAlreadyFeedback(user, record.getRecordId());

        // 불만족 피드백 처리
        if (feedbackDto.getSatisfaction() == Satisfaction.BAD) {
            checkCommentLength(feedbackDto.getComment());
            sendFeedbackAlarm(record, feedbackDto);
        }

        // 피드백 저장
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

    private void sendFeedbackAlarm(Record record, FeedbackRequest.FeedbackDto feedbackDto) {
        switch (feedbackDto.getFeedbackType()) {
            case FeedbackType.CHAT -> {
                List<Chat> chatHistory = chatDbService.findChatsByChatRoom(record.getChatRoom());
                feedbackAlarmSender.sendChatFeedbackAlarm(record, feedbackDto, chatHistory);
            }
            case FeedbackType.ANALYSIS -> {
                Analysis analysis = analysisDbService.findAnalysisById(record.getAnalysis().getAnalysisId());
                feedbackAlarmSender.sendAnalysisFeedbackAlarm(record, feedbackDto, analysis);
            }
        }
    }
}
