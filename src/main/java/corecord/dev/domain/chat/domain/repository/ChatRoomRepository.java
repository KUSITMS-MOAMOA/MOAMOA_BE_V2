package corecord.dev.domain.chat.domain.repository;

import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.user.domain.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByChatRoomIdAndUser(Long chatRoomId, User user);

    @Modifying
    @Query("DELETE " +
            "FROM ChatRoom cr " +
            "WHERE cr.user.userId IN :userId")
    void deleteChatRoomByUserId(@Param(value = "userId") Long userId);
}
