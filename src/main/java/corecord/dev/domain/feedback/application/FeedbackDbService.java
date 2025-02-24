package corecord.dev.domain.feedback.application;

import corecord.dev.domain.feedback.domain.entity.Feedback;
import corecord.dev.domain.feedback.domain.repository.FeedbackRepository;
import corecord.dev.domain.user.domain.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackDbService {
    private final FeedbackRepository feedbackRepository;

    @Transactional
    public void saveFeedback(Feedback feedback) {
        feedbackRepository.save(feedback);
    }

    @Transactional
    public boolean existsByUserAndRecordId(User user, Long recordId) {
        return feedbackRepository.existsByUserAndRecordId(user, recordId);
    }

    @Transactional
    public void deleteFeedbackByUserId(Long userId) {
        feedbackRepository.deleteByUserId(userId);
    }
}
