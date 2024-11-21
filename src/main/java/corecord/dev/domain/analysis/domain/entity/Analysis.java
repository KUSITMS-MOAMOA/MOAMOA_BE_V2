package corecord.dev.domain.analysis.domain.entity;

import corecord.dev.common.base.BaseEntity;
import corecord.dev.domain.ability.domain.entity.Ability;
import corecord.dev.domain.record.domain.entity.Record;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "analysis")
public class Analysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id", nullable = false)
    private Long analysisId;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "comment", nullable = false, length = 300)
    private String comment;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "record_id", nullable = true)
    private Record record;

    @BatchSize(size = 3)
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ability> abilityList;

    public void updateContent(String content) {
        if (content != null && !content.isEmpty())
            this.content = content;
    }

    public void updateComment(String comment) {
        if (!comment.isEmpty()) {
            this.comment = comment;
        }
    }

    public void addAbility(Ability ability) {
        if (ability != null) {
            this.abilityList.add(ability);
        }
    }

}
