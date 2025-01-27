package corecord.dev.domain.folder.domain.entity;

import corecord.dev.common.base.BaseEntity;
import corecord.dev.domain.record.domain.entity.Record;
import corecord.dev.domain.user.domain.entity.User;
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
@Table(name = "folder",
        indexes = {@Index(name = "user_created_at_idx", columnList = "user_id, created_at")})
public class Folder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id", nullable = false)
    private Long folderId;

    @Column(name = "title", nullable = false, length = 15)
    private String title;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Record> records;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void updateTitle(String title) {
        this.title = title;
    }
}
