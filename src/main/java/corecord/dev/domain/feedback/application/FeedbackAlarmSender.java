package corecord.dev.domain.feedback.application;

import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.feedback.domain.dto.request.FeedbackRequest;
import corecord.dev.domain.record.domain.entity.Record;

import java.util.List;

public interface FeedbackAlarmSender {
    void sendChatFeedbackAlarm(Record record, FeedbackRequest.FeedbackDto feedbackDto, List<Chat> chatList);
    void sendAnalysisFeedbackAlarm(Record record, FeedbackRequest.FeedbackDto feedbackDto, Analysis analysis);
}
