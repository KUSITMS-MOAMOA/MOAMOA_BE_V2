package corecord.dev.domain.analysis.entity;

import corecord.dev.common.base.BaseEntity;
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
public class Analysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long analysisId;

    @Column(nullable = false)
    private int count; // min=1, max=3

    @Column(nullable = false)
    private String suggestion;

}
