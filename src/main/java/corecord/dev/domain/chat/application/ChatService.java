package corecord.dev.domain.chat.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.domain.chat.domain.converter.ChatConverter;
import corecord.dev.domain.chat.domain.dto.request.ChatRequest;
import corecord.dev.domain.chat.domain.dto.response.ChatResponse;
import corecord.dev.domain.chat.infra.clova.dto.response.ChatSummaryAiResponse;
import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.chat.status.ChatErrorStatus;
import corecord.dev.domain.chat.exception.ChatException;
import corecord.dev.domain.chat.infra.clova.dto.request.ClovaRequest;
import corecord.dev.domain.chat.infra.clova.application.ClovaService;
import corecord.dev.domain.user.application.UserDbService;
import corecord.dev.domain.user.domain.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatDbService chatDbService;
    private final ClovaService clovaService;
    private final UserDbService userDbService;

    /*
     * user의 채팅방을 생성하고 생성된 채팅방 정보를 반환
     * @param userId
     * @return chatRoomDto
     */
    public ChatResponse.ChatRoomDto createChatRoom(Long userId) {
        User user = userDbService.findUserById(userId);

        // 채팅방 생성
        ChatRoom chatRoom = chatDbService.createChatRoom(user);

        // 첫번째 채팅 생성 - "안녕하세요! {nickName}님! {nickName}님의 경험이 궁금해요. {nickName}님의 경험을 들려주세요!"
        String firstChatContent = String.format("안녕하세요! %s님\n오늘은 어떤 경험을 했나요?\n저와 함께 정리해보아요!", user.getNickName());
        Chat firstChat = chatDbService.saveChat(0, firstChatContent, chatRoom);

        return ChatConverter.toChatRoomDto(chatRoom, firstChat);
    }

    /*
     * user의 채팅방에 채팅을 생성하고 생성된 채팅 정보와 AI 답변 반환
     * @param chatRoomId
     * @param chatDto
     * @return
     */
    public ChatResponse.ChatsDto createChat(Long userId, Long chatRoomId, ChatRequest.ChatDto chatDto) {
        User user = userDbService.findUserById(userId);
        ChatRoom chatRoom = chatDbService.findChatRoomById(chatRoomId, user);

        // 사용자 채팅 생성
        chatDbService.saveChat(1, chatDto.getContent(), chatRoom);

        // 가이드이면 가이드 채팅 생성
        if (chatDto.isGuide()) {
            checkGuideChat(chatRoom);
            return generateGuideChats(chatRoom);
        }

        // AI 답변 생성
        String aiAnswer = createChatAiAnswer(chatRoom, chatDto.getContent());
        Chat aiChat = chatDbService.saveChat(0, aiAnswer, chatRoom);

        return ChatConverter.toChatsDto(List.of(aiChat));
    }

    /*
     * user의 채팅방의 채팅 목록을 반환
     * @param userId
     * @param chatRoomId
     * @return chatListDto
     */
    public ChatResponse.ChatListDto getChatList(Long userId, Long chatRoomId) {
        User user = userDbService.findUserById(userId);
        ChatRoom chatRoom = chatDbService.findChatRoomById(chatRoomId, user);
        List<Chat> chatList = chatDbService.findChatsByChatRoom(chatRoom);

        return ChatConverter.toChatListDto(chatList);
    }

    /*
     * user의 채팅방을 삭제
     * @param userId
     * @param chatRoomId
     */
    public void deleteChatRoom(Long userId, Long chatRoomId) {
        User user = userDbService.findUserById(userId);
        ChatRoom chatRoom = chatDbService.findChatRoomById(chatRoomId, user);

        // 임시 저장된 ChatRoom 인지 확인 후 삭제
        checkTmpChat(user, chatRoom);
        chatDbService.deleteChatRoom(chatRoom);
    }

    /*
     * user의 채팅의 요약 정보를 반환
     * @param userId
     * @param chatRoomId
     * @return chatSummaryDto
     */
    public ChatResponse.ChatSummaryDto getChatSummary(Long userId, Long chatRoomId) {
        User user = userDbService.findUserById(userId);
        ChatRoom chatRoom = chatDbService.findChatRoomById(chatRoomId, user);
        List<Chat> chatList = chatDbService.findChatsByChatRoom(chatRoom);

        // 사용자 입력 없이 저장하려는 경우 체크
        validateChatList(chatList);

        // 채팅 정보 요약 생성
        ChatSummaryAiResponse response = generateChatSummary(chatList);

        validateResponse(response);

        return ChatConverter.toChatSummaryDto(chatRoom, response);
    }

    /*
     * user의 임시 채팅방과 유무를 반환
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

    /*
     * user의 채팅방을 임시 저장
     * @param userId
     * @param chatRoomId
     */
    @Transactional
    public void saveChatTmp(Long userId, Long chatRoomId) {
        User user = userDbService.findUserById(userId);
        ChatRoom chatRoom = chatDbService.findChatRoomById(chatRoomId, user);

        // 이미 임시 저장된 채팅방이 있는 경우
        if (user.getTmpChat() != null) {
            throw new ChatException(ChatErrorStatus.TMP_CHAT_EXIST);
        }
        userDbService.updateUserTmpChat(user, chatRoom.getChatRoomId());
    }

    private static void checkGuideChat(ChatRoom chatRoom) {
        if (chatRoom.getChatList().size() > 2) {
            throw new ChatException(ChatErrorStatus.INVALID_GUIDE_CHAT);
        }
    }

    private ChatResponse.ChatsDto generateGuideChats(ChatRoom chatRoom) {
        Chat guideChat1 = chatDbService.saveChat(0, "걱정 마세요!\n저와 대화하다 보면 경험이 정리될 거예요\uD83D\uDCDD", chatRoom);
        Chat guideChat2 = chatDbService.saveChat(0, "오늘은 어떤 경험을 했나요?\n상황과 해결한 문제를 말해주세요!", chatRoom);
        return ChatConverter.toChatsDto(List.of(guideChat1, guideChat2));
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

    private static void validateChatList(List<Chat> chatList) {
        if (chatList.size() <= 1) {
            throw new ChatException(ChatErrorStatus.NO_RECORD);
        }
    }

    private ChatSummaryAiResponse generateChatSummary(List<Chat> chatList) {
        ClovaRequest clovaRequest = ClovaRequest.createChatSummaryRequest(chatList);
        String response = clovaService.generateAiResponse(clovaRequest);
        return parseChatSummaryResponse(response);
    }

    private ChatSummaryAiResponse parseChatSummaryResponse(String aiResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(aiResponse, ChatSummaryAiResponse.class);
        } catch (JsonProcessingException e) {
            throw new ChatException(ChatErrorStatus.INVALID_CHAT_SUMMARY);
        }
    }

    private void checkTmpChat(User user, ChatRoom chatRoom) {
        if (user.getTmpChat() == null) {
            return;
        }
        if (user.getTmpChat().equals(chatRoom.getChatRoomId())) {
            user.deleteTmpChat();
        }
    }

    private String createChatAiAnswer(ChatRoom chatRoom, String userInput) {
        List<Chat> chatHistory = chatDbService.findChatsByChatRoom(chatRoom);
        ClovaRequest clovaRequest = ClovaRequest.createChatRequest(chatHistory, userInput);
        return clovaService.generateAiResponse(clovaRequest);
    }
}
