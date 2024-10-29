package corecord.dev.domain.analysis.entity;

import corecord.dev.common.base.BaseEntity;
import corecord.dev.domain.record.entity.Record;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Analysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long analysisId;

    @Column(nullable = false, length = 500)
    private String comment;

    @OneToOne
    @JoinColumn(name = "record_id", nullable = false)
    private Record record;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ability> abilityList;
}
