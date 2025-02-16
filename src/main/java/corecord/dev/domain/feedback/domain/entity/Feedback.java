package corecord.dev.domain.feedback.domain.entity;

import corecord.dev.common.base.BaseEntity;
import corecord.dev.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "feedback")
public class Feedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id", nullable = false)
    private Long feedbackId;

    @Column(name = "satisfaction", nullable = false)
    private Satisfaction satisfaction;

    @Column(name = "feedback_type", nullable = true)
    private FeedbackType feedbackType;

    @Column(name = "issue", nullable = true)
    private Issue issue;

    @Column(name = "comment", nullable = true, length = 500)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "chat_room_id", nullable = true)
    private Long chatRoomId;

    @Column(name = "analysis_id", nullable = true)
    private Long analysisId;

}
