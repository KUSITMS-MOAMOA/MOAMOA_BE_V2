package corecord.dev.domain.record.domain.entity;

import corecord.dev.common.base.BaseEntity;
import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.folder.domain.entity.Folder;
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
public class Record extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long recordId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordType type;

    @Column(length = 50)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
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
