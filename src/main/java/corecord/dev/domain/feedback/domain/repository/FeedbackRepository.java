package corecord.dev.domain.feedback.domain.repository;

import corecord.dev.domain.feedback.domain.entity.Feedback;
import corecord.dev.domain.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    boolean existsByUserAndRecordId(User user, Long recordId);

    @Modifying
    @Query("DELETE " +
            "FROM Feedback f " +
            "WHERE f.user.userId IN :userId")
    void deleteByUserId(@Param(value = "userId") Long userId);
}
