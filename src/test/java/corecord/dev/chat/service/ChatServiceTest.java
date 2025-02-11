package corecord.dev.chat.service;

import corecord.dev.domain.chat.application.ChatAIService;
import corecord.dev.domain.chat.application.ChatDbService;
import corecord.dev.domain.chat.application.ChatService;
import corecord.dev.domain.chat.domain.dto.request.ChatRequest;
import corecord.dev.domain.chat.domain.dto.response.ChatResponse;
import corecord.dev.domain.chat.domain.dto.response.ChatSummaryAiResponse;
import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.chat.exception.ChatException;
import corecord.dev.domain.user.application.UserDbService;
import corecord.dev.domain.user.domain.entity.Status;
import corecord.dev.domain.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private ChatDbService chatDbService;

    @Mock
    private UserDbService userDbService;

    @Mock
    private ChatAIService chatAIService;

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
        when(userDbService.findUserById(user.getUserId())).thenReturn(user);
        when(chatDbService.createChatRoom(user)).thenReturn(chatRoom);
        when(chatDbService.saveChat(anyInt(), anyString(), any(ChatRoom.class)))
                .thenAnswer(invocation -> createTestChat(invocation.getArgument(1), invocation.getArgument(0)));

        // When
        ChatResponse.ChatRoomDto result = chatService.createChatRoom(user.getUserId());

        // Then
        verify(chatDbService).createChatRoom(user);
        verify(chatDbService).saveChat(0, "안녕하세요! testUser님\n오늘은 어떤 경험을 했나요?\n저와 함께 정리해보아요!", chatRoom);
        assertEquals(result.getFirstChat(), "안녕하세요! testUser님\n오늘은 어떤 경험을 했나요?\n저와 함께 정리해보아요!");
    }

    @Test
    @DisplayName("채팅 조회 테스트")
    void getChatList() {
        // Given
        Chat userChat = createTestChat("userChat", 1);
        Chat aiChat = createTestChat("aiChat", 0);

        when(userDbService.findUserById(user.getUserId())).thenReturn(user);
        when(chatDbService.findChatRoomById(chatRoom.getChatRoomId(), user.getUserId())).thenReturn(chatRoom);
        when(chatDbService.findChatsByChatRoom(chatRoom)).thenReturn(List.of(userChat, aiChat));

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

            when(userDbService.findUserById(user.getUserId())).thenReturn(user);
            when(chatDbService.findChatRoomById(chatRoom.getChatRoomId(), user.getUserId())).thenReturn(chatRoom);
            when(chatDbService.saveChat(anyInt(), anyString(), any(ChatRoom.class)))
                    .thenAnswer(invocation -> createTestChat(invocation.getArgument(1), invocation.getArgument(0)));

            // When
            ChatResponse.ChatsDto result = chatService.createChat(
                    user.getUserId(),
                    chatRoom.getChatRoomId(),
                    request
            );

            // Then
            verify(chatDbService, times(3)).saveChat(anyInt(), anyString(), eq(chatRoom)); // 사용자 입력 1개, 가이드 2개
            assertEquals(result.getChats().size(), 2); // Guide 메시지는 두 개 생성
            assertEquals(result.getChats().get(0).getContent(), "아래 질문에 답하다 보면 경험이 정리될 거예요! \n" +
                    "S(상황) : 어떤 상황이었나요?\n" +
                    "T(과제) : 마주한 문제나 목표는 무엇이었나요?\n" +
                    "A(행동) : 문제를 해결하기 위해 어떻게 노력했나요?\n" +
                    "R(결과) : 그 결과는 어땠나요?");
            assertEquals(result.getChats().get(1).getContent(), "우선 기억나는 내용부터 가볍게 적어보세요.\n" +
                    "부족한 부분은 대화를 통해 모아모아가 도와줄게요! \uD83D\uDCDD");
        }

        @Test
        @DisplayName("AI 응답 성공 시")
        void createChatWithSuccess() {
            // Given
            ChatRequest.ChatDto request = ChatRequest.ChatDto.builder()
                    .content("테스트 입력")
                    .build();

            when(userDbService.findUserById(user.getUserId())).thenReturn(user);
            when(chatDbService.findChatRoomById(chatRoom.getChatRoomId(), user.getUserId())).thenReturn(chatRoom);
            when(chatDbService.saveChat(anyInt(), anyString(), any(ChatRoom.class)))
                    .thenAnswer(invocation -> createTestChat(invocation.getArgument(1), invocation.getArgument(0)));
            when(chatAIService.generateChatResponse(anyList(), anyString())).thenReturn("AI의 예상 응답");

            // When
            ChatResponse.ChatsDto result = chatService.createChat(
                    user.getUserId(),
                    chatRoom.getChatRoomId(),
                    request
            );

            // Then
            verify(chatDbService, times(2)).saveChat(anyInt(), anyString(), eq(chatRoom)); // 사용자 입력 1개, AI 응답 1개
            assertEquals(result.getChats().size(), 1);
            assertEquals(result.getChats().getFirst().getContent(), "AI의 예상 응답");
        }
    }

    @Nested
    @DisplayName("채팅 요약 정보 생성 테스트")
    class ChatSummaryTests {

        @Test
        @DisplayName("AI 응답 성공 시")
        void validAiResponse() {
            // Given
            List<Chat> chatList = List.of(
                    createTestChat("userChat1", 1),
                    createTestChat("aiChat1", 0),
                    createTestChat("userChat2", 1)
            );

            when(userDbService.findUserById(user.getUserId())).thenReturn(user);
            when(chatDbService.findChatRoomById(chatRoom.getChatRoomId(), user.getUserId())).thenReturn(chatRoom);
            when(chatDbService.findChatsByChatRoom(chatRoom)).thenReturn(chatList);
            when(chatAIService.generateChatSummaryResponse(anyList()))
                    .thenReturn(new ChatSummaryAiResponse("요약 제목", "요약 내용"));

            // When
            ChatResponse.ChatSummaryDto result = chatService.getChatSummary(user.getUserId(), chatRoom.getChatRoomId());

            // Then
            assertEquals(result.getTitle(), "요약 제목");
            assertEquals(result.getContent(), "요약 내용");
        }

        @Test
        @DisplayName("AI 응답이 빈 경우")
        void emptyAiResponse() {
            // Given
            List<Chat> chatList = List.of(
                    createTestChat("userChat1", 1),
                    createTestChat("aiChat1", 0)
            );

            when(userDbService.findUserById(user.getUserId())).thenReturn(user);
            when(chatDbService.findChatRoomById(chatRoom.getChatRoomId(), user.getUserId())).thenReturn(chatRoom);
            when(chatDbService.findChatsByChatRoom(chatRoom)).thenReturn(chatList);
            when(chatAIService.generateChatSummaryResponse(anyList()))
                    .thenReturn(new ChatSummaryAiResponse("", "")); // 빈 응답

            // When & Then
            assertThrows(ChatException.class, () -> chatService.getChatSummary(user.getUserId(), chatRoom.getChatRoomId()));
        }

        @Test
        @DisplayName("AI 응답이 긴 경우 예외 발생 (제목 50자 초과)")
        void longAiTitle() {
            // Given
            List<Chat> chatList = List.of(
                    createTestChat("userChat1", 1),
                    createTestChat("aiChat1", 0)
            );

            String longTitle = "a".repeat(51); // 51자 제목 생성
            when(userDbService.findUserById(user.getUserId())).thenReturn(user);
            when(chatDbService.findChatRoomById(chatRoom.getChatRoomId(), user.getUserId())).thenReturn(chatRoom);
            when(chatDbService.findChatsByChatRoom(chatRoom)).thenReturn(chatList);
            when(chatAIService.generateChatSummaryResponse(anyList()))
                    .thenReturn(new ChatSummaryAiResponse(longTitle, "정상 내용")); // 50자 초과 제목

            // When & Then
            assertThrows(ChatException.class, () -> chatService.getChatSummary(user.getUserId(), chatRoom.getChatRoomId()));
        }

        @Test
        @DisplayName("AI 응답이 긴 경우 예외 발생 (내용 500자 초과)")
        void longAiResponse() {
            // Given
            List<Chat> chatList = List.of(
                    createTestChat("userChat1", 1),
                    createTestChat("aiChat1", 0)
            );

            String longContent = "a".repeat(501); // 501자 응답 생성
            when(userDbService.findUserById(user.getUserId())).thenReturn(user);
            when(chatDbService.findChatRoomById(chatRoom.getChatRoomId(), user.getUserId())).thenReturn(chatRoom);
            when(chatDbService.findChatsByChatRoom(chatRoom)).thenReturn(chatList);
            when(chatAIService.generateChatSummaryResponse(anyList()))
                    .thenReturn(new ChatSummaryAiResponse("정상 제목", longContent)); // 500자 초과 내용

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
            when(userDbService.findUserById(user.getUserId())).thenReturn(user);
            when(chatDbService.findChatRoomById(chatRoom.getChatRoomId(), user.getUserId())).thenReturn(chatRoom);

            // When
            chatService.saveChatTmp(user.getUserId(), chatRoom.getChatRoomId());

            // Then
            verify(userDbService).findUserById(user.getUserId());
            verify(chatDbService).findChatRoomById(chatRoom.getChatRoomId(), user.getUserId());
            verify(userDbService).updateUserTmpChat(user, chatRoom.getChatRoomId());
        }

        @Test
        @DisplayName("이미 임시 저장된 채팅방이 있을 경우 예외 처리")
        void saveChatTmpFailsWhenTmpChatExists() {
            // Given
            user.updateTmpChat(chatRoom.getChatRoomId());
            when(userDbService.findUserById(user.getUserId())).thenReturn(user);
            when(chatDbService.findChatRoomById(chatRoom.getChatRoomId(), user.getUserId())).thenReturn(chatRoom);

            // When & Then
            assertThrows(ChatException.class, () -> chatService.saveChatTmp(user.getUserId(), chatRoom.getChatRoomId()));
        }

        @Test
        @DisplayName("조회 성공")
        void getChatTmp() {
            // Given
            user.updateTmpChat(chatRoom.getChatRoomId());
            when(userDbService.findUserById(user.getUserId())).thenReturn(user);

            // When
            ChatResponse.ChatTmpDto result = chatService.getChatTmp(user.getUserId());

            // Then
            assertEquals(result.getChatRoomId(), chatRoom.getChatRoomId());
            assertTrue(result.isExist());
            verify(userDbService).findUserById(user.getUserId());
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

    private Chat createTestChat(String content, int isSystem) {
        Chat chat = Chat.builder()
                .chatId(1L)
                .author(isSystem)
                .content(content)
                .chatRoom(chatRoom)
                .build();
        chat.setCreatedAt(LocalDateTime.now());
        return chat;
    }
}
