package corecord.dev.domain.chat.repository;

import corecord.dev.domain.chat.entity.Chat;
import corecord.dev.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByChatRoomOrderByChatId(ChatRoom chatRoom);
}
