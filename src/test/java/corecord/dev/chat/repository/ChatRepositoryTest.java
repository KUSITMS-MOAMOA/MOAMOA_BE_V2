package corecord.dev.chat.repository;

import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.chat.domain.repository.ChatRepository;
import corecord.dev.domain.chat.domain.repository.ChatRoomRepository;
import corecord.dev.domain.user.domain.enums.Status;
import corecord.dev.domain.user.domain.entity.User;
import corecord.dev.domain.user.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChatRepositoryTest {

    @Autowired
    ChatRepository chatRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("채팅방 ID로 채팅 삭제 테스트")
    void deleteByChatRoomId() {
        // Given
        User user = createTestUser();
        ChatRoom chatRoom = createTestChatRoom(user);

        createTestChat(chatRoom, "First message");
        createTestChat(chatRoom, "Second message");

        List<Chat> chatsBeforeDelete = chatRepository.findByChatRoomOrderByChatId(chatRoom);
        assertEquals(2, chatsBeforeDelete.size());

        // When
        chatRepository.deleteByChatRoomId(chatRoom.getChatRoomId());
        entityManager.flush();

        // Then
        List<Chat> chatsAfterDelete = chatRepository.findByChatRoomOrderByChatId(chatRoom);
        assertTrue(chatsAfterDelete.isEmpty());
    }

    @Test
    @DisplayName("채팅방에 속한 채팅 조회 테스트")
    void findByChatRoomOrderByChatId() {
        // Given
        User user = createTestUser();
        ChatRoom chatRoom = createTestChatRoom(user);

        createTestChat(chatRoom, "First message");
        createTestChat(chatRoom, "Second message");
        createTestChat(chatRoom, "Third message");

        // When
        List<Chat> chats = chatRepository.findByChatRoomOrderByChatId(chatRoom);

        // Then
        assertEquals(3, chats.size());
        assertEquals("First message", chats.get(0).getContent());
        assertEquals("Second message", chats.get(1).getContent());
        assertEquals("Third message", chats.get(2).getContent());
    }

    private User createTestUser() {
        // 사용자 저장 시 persist 호출
        User user = User.builder()
                .providerId("testProvider")
                .nickName("TestUser")
                .status(Status.UNIVERSITY_STUDENT)
                .build();
        userRepository.save(user);
        return user;
    }

    private ChatRoom createTestChatRoom(User user) {
        ChatRoom chatRoom = ChatRoom.builder()
                .user(user)
                .build();
        chatRoomRepository.save(chatRoom);
        return chatRoom;
    }

    private void createTestChat(ChatRoom chatRoom, String content) {
        Chat chat = Chat.builder()
                .author(1)
                .content(content)
                .chatRoom(chatRoom)
                .build();
        chatRepository.save(chat);
    }
}
