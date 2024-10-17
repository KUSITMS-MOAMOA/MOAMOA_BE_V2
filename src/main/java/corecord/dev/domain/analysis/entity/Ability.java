package corecord.dev.domain.analysis.entity;

import corecord.dev.common.base.BaseEntity;
import corecord.dev.domain.analysis.constant.Keyword;
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

    @Column(nullable = false)
    private String content;
}
