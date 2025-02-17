package corecord.dev.domain.chat.application;

import corecord.dev.domain.chat.domain.converter.ChatConverter;
import corecord.dev.domain.chat.domain.dto.request.ChatRequest;
import corecord.dev.domain.chat.domain.dto.response.ChatResponse;
import corecord.dev.domain.chat.domain.dto.response.ChatSummaryAiResponse;
import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.chat.exception.ChatException;
import corecord.dev.domain.chat.status.ChatErrorStatus;
import corecord.dev.domain.user.application.UserDbService;
import corecord.dev.domain.user.domain.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatDbService chatDbService;
    private final ChatAIService chatAIService;
    private final UserDbService userDbService;

    /**
     * user의 채팅방을 생성하고 생성된 채팅방 정보를 반환합니다.
     *
     * @param userId
     * @return chatRoomId, 첫 번째 메시지
     */
    @Override
    public ChatResponse.ChatRoomDto createChatRoom(Long userId) {
        User user = userDbService.findUserById(userId);

        // 채팅방 생성
        ChatRoom chatRoom = chatDbService.createChatRoom(user);

        // 첫번째 채팅 생성
        String firstChatContent = String.format("안녕하세요! %s님의 경험을 말해주세요.\n어떤 경험을 했나요? 당시 상황과 문제를 해결하기 위한 %s님의 노력이 궁금해요", user.getNickName(), user.getNickName());
        Chat firstChat = chatDbService.saveChat(0, firstChatContent, chatRoom);

        return ChatConverter.toChatRoomDto(chatRoom, firstChat);
    }

    /**
     * user의 채팅방에 채팅을 생성하고 생성된 채팅 정보와 AI 답변 반환합니다.
     *
     * @param chatRoomId
     * @param chatDto
     * @return 각 chat의 chatId, content를 담은 리스트
     */
    @Override
    public ChatResponse.ChatsDto createChat(Long userId, Long chatRoomId, ChatRequest.ChatDto chatDto) {
        ChatRoom chatRoom = chatDbService.findChatRoomById(chatRoomId, userId);

        // 사용자 채팅 생성
        chatDbService.saveChat(1, chatDto.getContent(), chatRoom);

        // 가이드이면 가이드 채팅 생성
        if (chatDto.isGuide()) {
            checkGuideChat(chatRoom);
            return generateGuideChats(chatRoom);
        }

        // AI 답변 생성
        List<Chat> chatHistory = chatDbService.findChatsByChatRoom(chatRoom);
        String aiAnswer = chatAIService.generateChatResponse(chatHistory, chatDto.getContent());
        Chat aiChat = chatDbService.saveChat(0, aiAnswer, chatRoom);

        return ChatConverter.toChatsDto(List.of(aiChat));
    }

    private static void checkGuideChat(ChatRoom chatRoom) {
        if (chatRoom.getChatList().size() > 2)
            throw new ChatException(ChatErrorStatus.INVALID_GUIDE_CHAT);
    }

    private ChatResponse.ChatsDto generateGuideChats(ChatRoom chatRoom) {
        Chat guideChat1 = chatDbService.saveChat(0, "아래 질문에 답하다 보면 경험이 정리될 거예요! \n" +
                "S(상황) : 어떤 상황이었나요?\n" +
                "T(과제) : 마주한 문제나 목표는 무엇이었나요?\n" +
                "A(행동) : 문제를 해결하기 위해 어떻게 노력했나요?\n" +
                "R(결과) : 그 결과는 어땠나요?", chatRoom);
        Chat guideChat2 = chatDbService.saveChat(0, "우선 기억나는 내용부터 가볍게 적어보세요.\n" +
                "부족한 부분은 대화를 통해 모아모아가 도와줄게요! \uD83D\uDCDD", chatRoom);
        return ChatConverter.toChatsDto(List.of(guideChat1, guideChat2));
    }

    /**
     * user의 채팅방의 채팅 목록을 반환합니다.
     *
     * @param userId
     * @param chatRoomId
     * @return 각 chat의 chatId, 저자, content, 생성된 기점을 담은 리스트
     */
    @Override
    public ChatResponse.ChatListDto getChatList(Long userId, Long chatRoomId) {
        ChatRoom chatRoom = chatDbService.findChatRoomById(chatRoomId, userId);
        List<Chat> chatList = chatDbService.findChatsByChatRoom(chatRoom);

        return ChatConverter.toChatListDto(chatList);
    }

    /**
     * user의 채팅방을 삭제합니다.
     *
     * @param userId
     * @param chatRoomId
     */
    @Override
    public void deleteChatRoom(Long userId, Long chatRoomId) {
        User user = userDbService.findUserById(userId);
        ChatRoom chatRoom = chatDbService.findChatRoomById(chatRoomId, userId);

        // 임시 저장된 ChatRoom 인지 확인 후 삭제
        checkTmpChat(user, chatRoom);
        chatDbService.deleteChatRoom(chatRoom);
    }

    private void checkTmpChat(User user, ChatRoom chatRoom) {
        if (user.getTmpChat() == null) {
            return;
        }
        if (user.getTmpChat().equals(chatRoom.getChatRoomId())) {
            user.deleteTmpChat();
        }
    }

    /**
     * user의 채팅의 요약 정보를 반환합니다.
     *
     * @param userId
     * @param chatRoomId
     * @return chat 요약 결과(제목, 본문)
     */
    public ChatResponse.ChatSummaryDto getChatSummary(Long userId, Long chatRoomId) {
        ChatRoom chatRoom = chatDbService.findChatRoomById(chatRoomId, userId);
        List<Chat> chatList = chatDbService.findChatsByChatRoom(chatRoom);

        // 사용자 입력 없이 저장하려는 경우 체크
        validateChatList(chatList);

        // 채팅 정보 요약 생성
        ChatSummaryAiResponse response = chatAIService.generateChatSummaryResponse(chatList);
        validateResponse(response);

        return ChatConverter.toChatSummaryDto(chatRoom, response);
    }

    private static void validateChatList(List<Chat> chatList) {
        if (chatList.size() <= 1)
            throw new ChatException(ChatErrorStatus.NO_RECORD);
    }

    private static void validateResponse(ChatSummaryAiResponse response) {
        if (response.getTitle().equals("NO_RECORD") || response.getContent().equals("NO_RECORD") || response.getContent().equals("") || response.getTitle().equals("")) {
            throw new ChatException(ChatErrorStatus.NO_RECORD);
        }

        if (response.getTitle().length() > 30) {
            throw new ChatException(ChatErrorStatus.OVERFLOW_SUMMARY_TITLE);
        }

        if (response.getContent().length() > 500) {
            throw new ChatException(ChatErrorStatus.OVERFLOW_SUMMARY_CONTENT);
        }
    }

    /**
     * user의 임시 채팅방과 임시 채팅 저장 유무를 반환합니다.
     *
     * @param userId
     * @return chatTmpDto
     */
    @Transactional
    public ChatResponse.ChatTmpDto getChatTmp(Long userId) {
        User user = userDbService.findUserById(userId);
        if (user.getTmpChat() == null) {
            return ChatConverter.toNotExistingChatTmpDto();
        }

        // 임시 채팅 제거 후 반환
        Long chatRoomId = user.getTmpChat();
        userDbService.deleteUserTmpChat(user);

        return ChatConverter.toExistingChatTmpDto(chatRoomId);
    }

    /**
     * user의 채팅방을 임시 저장합니다.
     *
     * @param userId
     * @param chatRoomId
     */
    @Transactional
    public void saveChatTmp(Long userId, Long chatRoomId) {
        User user = userDbService.findUserById(userId);
        ChatRoom chatRoom = chatDbService.findChatRoomById(chatRoomId, userId);

        // 이미 임시 저장된 채팅방이 있는 경우
        if (user.getTmpChat() != null) {
            throw new ChatException(ChatErrorStatus.TMP_CHAT_EXIST);
        }
        userDbService.updateUserTmpChat(user, chatRoom.getChatRoomId());
    }

}
