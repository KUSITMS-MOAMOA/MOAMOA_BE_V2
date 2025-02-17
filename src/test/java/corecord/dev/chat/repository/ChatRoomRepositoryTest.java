package corecord.dev.chat.repository;

import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.chat.domain.repository.ChatRoomRepository;
import corecord.dev.domain.user.domain.entity.Status;
import corecord.dev.domain.user.domain.entity.User;
import corecord.dev.domain.user.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChatRoomRepositoryTest {

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("ChatRoom ID와 User로 채팅방 조회 테스트")
    void findByChatRoomIdAndUser() {
        // Given
        User user = createTestUser();
        ChatRoom chatRoom = createTestChatRoom(user);

        // When
        Optional<ChatRoom> foundChatRoom = chatRoomRepository.findByChatRoomIdAndUserId(chatRoom.getChatRoomId(), user.getUserId());

        // Then
        assertTrue(foundChatRoom.isPresent());
        assertEquals(foundChatRoom.get().getUser(), user);
    }

    @Test
    @DisplayName("존재하지 않는 채팅방 조회 테스트")
    void findByChatRoomIdAndUser_NotFound() {
        // Given
        User user = createTestUser();

        // When
        Optional<ChatRoom> foundChatRoom = chatRoomRepository.findByChatRoomIdAndUserId(999L, user.getUserId());

        // Then
        assertFalse(foundChatRoom.isPresent());
    }

    private User createTestUser() {
        User user = User.builder()
                .providerId("testProvider")
                .nickName("TestUser")
                .status(Status.UNIVERSITY_STUDENT)
                .build();
        return userRepository.save(user);
    }

    private ChatRoom createTestChatRoom(User user) {
        ChatRoom chatRoom = ChatRoom.builder()
                .user(user)
                .build();
        return chatRoomRepository.save(chatRoom);
    }
}
