package corecord.dev.domain.ability.domain.entity;

import corecord.dev.common.base.BaseEntity;
import corecord.dev.domain.ability.domain.enums.Keyword;
import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ability",
        indexes = {@Index(name = "user_is_example_keyword_created_idx", columnList = "user_id, is_example, keyword, created_at"),
                @Index(name = "ability_keyword_analysis_idx", columnList = "user_id, keyword, is_example, analysis_id, created_at"),
                @Index(name = "ability_analysis_idx", columnList = "analysis_id, user_id, keyword")})
public class Ability extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ability_id", nullable = false)
    private Long abilityId;

    @Column(name = "keyword", nullable = false)
    @Enumerated(EnumType.STRING)
    private Keyword keyword;

    @Column(name = "content", nullable = false, length = 300)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private Analysis analysis;

    @Column(name = "is_example")
    @ColumnDefault("'0'")
    private char isExample;

    public void updateContent(String content) {
        if (content != null && !content.isEmpty()) {
            this.content = content;
        }
    }
}
