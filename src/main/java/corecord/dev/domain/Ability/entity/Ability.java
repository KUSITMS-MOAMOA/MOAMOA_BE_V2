package corecord.dev.domain.Ability.entity;

import corecord.dev.common.base.BaseEntity;
import corecord.dev.domain.analysis.entity.Analysis;
import corecord.dev.domain.user.entity.User;
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
public class Ability extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long abilityId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Keyword keyword;

    @Column(nullable = false, length = 200)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private Analysis analysis;

    public void updateContent(String content) {
        if (content != null && !content.isEmpty()) {
            this.content = content;
        }
    }
}
