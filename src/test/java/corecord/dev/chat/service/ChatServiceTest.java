package corecord.dev.chat.service;

import corecord.dev.domain.chat.dto.request.ChatRequest;
import corecord.dev.domain.chat.dto.response.ChatResponse;
import corecord.dev.domain.chat.entity.Chat;
import corecord.dev.domain.chat.entity.ChatRoom;
import corecord.dev.domain.chat.exception.model.ChatException;
import corecord.dev.domain.chat.repository.ChatRepository;
import corecord.dev.domain.chat.repository.ChatRoomRepository;
import corecord.dev.domain.chat.service.ChatService;
import corecord.dev.domain.chat.service.ClovaRequest;
import corecord.dev.domain.chat.service.ClovaService;
import corecord.dev.domain.user.entity.Status;
import corecord.dev.domain.user.entity.User;
import corecord.dev.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClovaService clovaService;

    private User user;

    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        user = createTestUser();
        chatRoom = createTestChatRoom();

    }

    @Test
    @DisplayName("채팅방 생성 테스트")
    void createChatRoom() {
        // Given
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

        // When
        ChatResponse.ChatRoomDto result = chatService.createChatRoom(user.getUserId());

        // Then
        verify(chatRoomRepository).save(any(ChatRoom.class));
        verify(chatRepository).save(any(Chat.class));
        assertEquals(result.getFirstChat(), "안녕하세요! testUser님\n오늘은 어떤 경험을 했나요?\n저와 함께 정리해보아요!");
    }

    @Test
    @DisplayName("채팅 조회 테스트")
    void getChatList() throws NoSuchFieldException, IllegalAccessException {
        // Given
        Chat userChat = createTestChat("userChat", 1);
        Chat aiChat = createTestChat("aiChat", 0);


        when(chatRepository.findByChatRoomOrderByChatId(chatRoom)).thenReturn(List.of(userChat, aiChat));
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(chatRoomRepository.findByChatRoomIdAndUser(chatRoom.getChatRoomId(), user)).thenReturn(Optional.of(chatRoom));

        // When
        ChatResponse.ChatListDto result = chatService.getChatList(user.getUserId(), chatRoom.getChatRoomId());

        // Then
        assertEquals(result.getChats().size(), 2);
        assertEquals(result.getChats().get(0).getContent(), "userChat");
        assertEquals(result.getChats().get(1).getContent(), "aiChat");
    }

    @Nested
    @DisplayName("채팅 생성하기 테스트")
    class ChatServiceAiResponseTests {

        @Test
        @DisplayName("가이드 호출 시")
        void createChatWithGuide() {
            // Given
            ChatRequest.ChatDto request = ChatRequest.ChatDto.builder()
                    .guide(true)
                    .content("어떤 경험을 말해야 할지 모르겠어요.")
                    .build();

            when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
            when(chatRoomRepository.findByChatRoomIdAndUser(chatRoom.getChatRoomId(), user)).thenReturn(Optional.of(chatRoom));

            // When
            ChatResponse.ChatsDto result = chatService.createChat(
                    user.getUserId(),
                    chatRoom.getChatRoomId(),
                    request
            );

            // Then
            verify(chatRepository, times(3)).save(any(Chat.class)); // 사용자 입력 1개, 가이드 2개
            assertEquals(result.getChats().size(), 2); // Guide 메시지는 두 개 생성
            assertEquals(result.getChats().get(0).getContent(), "걱정 마세요!\n저와 대화하다 보면 경험이 정리될 거예요\uD83D\uDCDD");
            assertEquals(result.getChats().get(1).getContent(), "오늘은 어떤 경험을 했나요?\n상황과 해결한 문제를 말해주세요!");
        }

        @Test
        @DisplayName("AI 응답 성공 시")
        void createChatWithSuccess() {
            // Given
            ChatRequest.ChatDto request = ChatRequest.ChatDto.builder()
                    .content("테스트 입력")
                    .build();

            when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
            when(chatRoomRepository.findByChatRoomIdAndUser(chatRoom.getChatRoomId(), user)).thenReturn(Optional.of(chatRoom));
            when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> invocation.getArgument(0)); // 저장된 Chat 객체 반환
            when(clovaService.generateAiResponse(any(ClovaRequest.class))).thenReturn("AI의 예상 응답");

            // When
            ChatResponse.ChatsDto result = chatService.createChat(
                    user.getUserId(),
                    chatRoom.getChatRoomId(),
                    request
            );

            // Then
            verify(chatRepository, times(2)).save(any(Chat.class)); // 사용자 입력 1개, AI 응답 1개
            assertEquals(result.getChats().size(), 1);
            assertEquals(result.getChats().getFirst().getContent(), "AI의 예상 응답");
        }
    }

    @Nested
    @DisplayName("채팅 요약 정보 생성 테스트")
    class ChatSummaryTests {

        @Test
        @DisplayName("AI 응답 성공 시")
        void validAiResponse() throws NoSuchFieldException, IllegalAccessException {
            // Given
            List<Chat> chatList = List.of(
                    createTestChat("userChat1", 1),
                    createTestChat("aiChat1", 0),
                    createTestChat("userChat2", 1)
            );

            when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
            when(chatRoomRepository.findByChatRoomIdAndUser(chatRoom.getChatRoomId(), user)).thenReturn(Optional.of(chatRoom));
            when(chatRepository.findByChatRoomOrderByChatId(chatRoom)).thenReturn(chatList);
            when(clovaService.generateAiResponse(any(ClovaRequest.class)))
                    .thenReturn("{\"title\":\"요약 제목\",\"content\":\"요약 내용\"}");

            // When
            ChatResponse.ChatSummaryDto result = chatService.getChatSummary(user.getUserId(), chatRoom.getChatRoomId());

            // Then
            assertEquals(result.getTitle(), "요약 제목");
            assertEquals(result.getContent(), "요약 내용");
        }

        @Test
        @DisplayName("AI 응답이 빈 경우")
        void emptyAiResponse() throws NoSuchFieldException, IllegalAccessException {
            // Given
            List<Chat> chatList = List.of(
                    createTestChat("userChat1", 1),
                    createTestChat("aiChat1", 0)
            );

            when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
            when(chatRoomRepository.findByChatRoomIdAndUser(chatRoom.getChatRoomId(), user)).thenReturn(Optional.of(chatRoom));
            when(chatRepository.findByChatRoomOrderByChatId(chatRoom)).thenReturn(chatList);
            when(clovaService.generateAiResponse(any(ClovaRequest.class)))
                    .thenReturn("{\"title\":\"\",\"content\":\"\"}"); // 빈 응답

            // When & Then
            assertThrows(ChatException.class, () -> chatService.getChatSummary(user.getUserId(), chatRoom.getChatRoomId()));
        }

        @Test
        @DisplayName("AI 응답이 긴 경우 예외 발생 (제목 50자 초과)")
        void longAiTitle() throws NoSuchFieldException, IllegalAccessException {
            // Given
            List<Chat> chatList = List.of(
                    createTestChat("userChat1", 1),
                    createTestChat("aiChat1", 0)
            );

            String longTitle = "a".repeat(51); // 51자 제목 생성
            when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
            when(chatRoomRepository.findByChatRoomIdAndUser(chatRoom.getChatRoomId(), user)).thenReturn(Optional.of(chatRoom));
            when(chatRepository.findByChatRoomOrderByChatId(chatRoom)).thenReturn(chatList);
            when(clovaService.generateAiResponse(any(ClovaRequest.class)))
                    .thenReturn(String.format("{\"title\":\"%s\",\"content\":\"정상 내용\"}", longTitle)); // 50자 초과 제목

            // When & Then
            assertThrows(ChatException.class, () -> chatService.getChatSummary(user.getUserId(), chatRoom.getChatRoomId()));
        }

        @Test
        @DisplayName("AI 응답이 긴 경우 예외 발생 (내용 500자 초과)")
        void longAiResponse() throws NoSuchFieldException, IllegalAccessException {
            // Given
            List<Chat> chatList = List.of(
                    createTestChat("userChat1", 1),
                    createTestChat("aiChat1", 0)
            );

            String longContent = "a".repeat(501); // 501자 응답 생성
            when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
            when(chatRoomRepository.findByChatRoomIdAndUser(chatRoom.getChatRoomId(), user)).thenReturn(Optional.of(chatRoom));
            when(chatRepository.findByChatRoomOrderByChatId(chatRoom)).thenReturn(chatList);
            when(clovaService.generateAiResponse(any(ClovaRequest.class)))
                    .thenReturn(String.format("{\"title\":\"정상 제목\",\"content\":\"%s\"}", longContent)); // 500자 초과 내용

            // When & Then
            assertThrows(ChatException.class, () -> chatService.getChatSummary(user.getUserId(), chatRoom.getChatRoomId()));
        }
    }

    @Nested
    @DisplayName("임시 채팅방 테스트")
    class ChatTmpTests {

        @Test
        @DisplayName("저장 성공")
        void saveChatTmp() {
            // Given
            when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
            when(chatRoomRepository.findByChatRoomIdAndUser(chatRoom.getChatRoomId(), user)).thenReturn(Optional.of(chatRoom));

            // When
            chatService.saveChatTmp(user.getUserId(), chatRoom.getChatRoomId());

            // Then
            assertEquals(user.getTmpChat(), chatRoom.getChatRoomId());
            verify(userRepository).findById(user.getUserId());
        }

        @Test
        @DisplayName("이미 임시 저장된 채팅방이 있을 경우 예외 처리")
        void saveChatTmpFailsWhenTmpChatExists() {
            // Given
            user.updateTmpChat(chatRoom.getChatRoomId());
            when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
            when(chatRoomRepository.findByChatRoomIdAndUser(chatRoom.getChatRoomId(), user)).thenReturn(Optional.of(chatRoom));

            // When & Then
            assertThrows(ChatException.class, () -> chatService.saveChatTmp(user.getUserId(), chatRoom.getChatRoomId()));
        }

        @Test
        @DisplayName("조회 성공")
        void getChatTmp() {
            // Given
            user.updateTmpChat(chatRoom.getChatRoomId());
            when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

            // When
            ChatResponse.ChatTmpDto result = chatService.getChatTmp(user.getUserId());

            // Then
            assertEquals(result.getChatRoomId(), chatRoom.getChatRoomId());
            assertTrue(result.isExist());
            verify(userRepository).findById(user.getUserId());
        }
    }

    private User createTestUser() {
        return User.builder()
                .userId(1L)
                .providerId("providerId")
                .nickName("testUser")
                .status(Status.UNIVERSITY_STUDENT)
                .abilities(new ArrayList<>())
                .chatRooms(new ArrayList<>())
                .folders(new ArrayList<>())
                .records(new ArrayList<>())
                .build();
    }

    private ChatRoom createTestChatRoom() {
        return ChatRoom.builder()
                .chatRoomId(1L)
                .user(user)
                .chatList(new ArrayList<>())
                .build();
    }

    private Chat createTestChat(String content, int isSystem) throws IllegalAccessException, NoSuchFieldException {
        Chat chat =  Chat.builder()
                .chatId(1L)
                .author(isSystem)
                .content(content)
                .chatRoom(chatRoom)
                .build();
        Field createdAtField = Chat.class.getSuperclass().getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        createdAtField.set(chat, LocalDateTime.now());
        return chat;
    }
}
