package corecord.dev.domain.user.domain.entity;

import corecord.dev.common.base.BaseEntity;
import corecord.dev.domain.ability.domain.entity.Ability;
import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.record.domain.entity.Record;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Setter
    @Column(name = "nick_name", nullable = false)
    private String nickName;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "tmp_chat")
    private Long tmpChat;

    @Column(name = "tmp_memo")
    private Long tmpMemo;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Record> records;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChatRoom> chatRooms;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Ability> abilities;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Folder> folders;

    public void updateTmpMemo(Long tmpMemo) {
        this.tmpMemo = tmpMemo;
    }

    public void updateTmpChat(Long tmpChat) {
        this.tmpChat = tmpChat;
    }

    public void deleteTmpMemo(){
        this.tmpMemo = null;
    }

    public void deleteTmpChat(){
        this.tmpChat = null;
    }
}
