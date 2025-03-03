package corecord.dev.domain.chat.domain.repository;

import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.chat.domain.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByChatRoomOrderByChatId(ChatRoom chatRoom);

    @Modifying
    @Query("DELETE FROM Chat c WHERE c.chatRoom.chatRoomId = :chatRoomId")
    void deleteByChatRoomId(Long chatRoomId);

    @Modifying
    @Query("DELETE " +
            "FROM Chat c " +
            "WHERE c.chatRoom.user.userId IN :userId")
    void deleteChatByUserId(@Param(value = "userId") Long userId);

    @Modifying
    @Query(value = "DELETE c, cr, r " +
            "FROM chat c " +
            "JOIN chat_room cr ON cr.chat_room_id = c.chat_room_id " +
            "JOIN record r ON r.chat_room_id = cr.chat_room_id " +
            "WHERE r.folder_id = :folderId ",
            nativeQuery = true)
    void deleteChatByFolderId(@Param(value = "folderId") Long folderId);

}
