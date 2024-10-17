package corecord.dev.domain.record.entity;

import corecord.dev.common.base.BaseEntity;
import corecord.dev.domain.record.constant.RecordType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor
public class Record extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long recordId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordType type;

    @Column(length = 15)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

}
