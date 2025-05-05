package corecord.dev.domain.record.domain.entity;

import corecord.dev.common.base.BaseEntity;
import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.record.domain.enums.RecordType;
import corecord.dev.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "record",
        indexes = {@Index(name = "user_created_at_idx", columnList = "user_id, created_at desc, record_id")})
public class Record extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id",nullable = false)
    private Long recordId;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordType type;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "chat_room_id", nullable = true)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = true)
    private Folder folder;

    @OneToOne(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Analysis analysis;

    public void updateFolder(Folder folder) {
        this.folder = folder;
    }

    public void updateTitle(String title) {
        if (title != null && !title.isEmpty()) {
            this.title = title;
        }
    }

    public boolean isMemoType() {
        return this.type == RecordType.MEMO;
    }
}
