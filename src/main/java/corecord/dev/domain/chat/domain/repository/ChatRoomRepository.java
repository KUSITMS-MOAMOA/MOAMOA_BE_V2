package corecord.dev.domain.chat.domain.repository;

import corecord.dev.domain.chat.domain.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr " +
            "FROM ChatRoom cr " +
            "JOIN FETCH cr.user u " +
            "WHERE cr.chatRoomId = :chatRoomId " +
            "AND u.userId = :userId ")
    Optional<ChatRoom> findByChatRoomIdAndUserId(@Param(value = "chatRoomId") Long chatRoomId,
                                                 @Param(value = "userId") Long userId);

    @Modifying
    @Query("DELETE " +
            "FROM ChatRoom cr " +
            "WHERE cr.user.userId IN :userId")
    void deleteChatRoomByUserId(@Param(value = "userId") Long userId);

    @Modifying
    @Query("DELETE " +
            "FROM ChatRoom cr " +
            "WHERE cr.chatRoomId = :chatRoomId")
    void deleteById(@Param(value = "chatRoomId") Long chatRoomId);
}
