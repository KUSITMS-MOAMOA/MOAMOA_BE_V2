package corecord.dev.domain.chat.repository;

import corecord.dev.domain.chat.entity.ChatRoom;
import corecord.dev.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @EntityGraph(attributePaths = {"record", "user"})
    Optional<ChatRoom> findByChatRoomIdAndUser(Long chatRoomId, User user);
}
