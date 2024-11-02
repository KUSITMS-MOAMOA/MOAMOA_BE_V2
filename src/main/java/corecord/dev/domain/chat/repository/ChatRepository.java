package corecord.dev.domain.chat.repository;

import corecord.dev.domain.chat.entity.Chat;
import corecord.dev.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByChatRoomOrderByChatId(ChatRoom chatRoom);

    @Modifying
    @Query("DELETE FROM Chat c WHERE c.chatRoom.chatRoomId = :chatRoomId")
    void deleteByChatRoomId(Long chatRoomId);
}
