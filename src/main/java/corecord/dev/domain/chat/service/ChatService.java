package corecord.dev.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.domain.chat.converter.ChatConverter;
import corecord.dev.domain.chat.dto.request.ChatRequest;
import corecord.dev.domain.chat.dto.response.ChatResponse;
import corecord.dev.domain.chat.dto.response.ChatSummaryAiResponse;
import corecord.dev.domain.chat.entity.Chat;
import corecord.dev.domain.chat.entity.ChatRoom;
import corecord.dev.domain.chat.exception.enums.ChatErrorStatus;
import corecord.dev.domain.chat.exception.model.ChatException;
import corecord.dev.domain.chat.repository.ChatRepository;
import corecord.dev.domain.chat.repository.ChatRoomRepository;
import corecord.dev.domain.user.entity.User;
import corecord.dev.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ClovaService clovaService;

    /*
     * user의 채팅방을 생성하고 생성된 채팅방 정보를 반환
     * @param userId
     * @return chatRoomDto
     */
    @Transactional
    public ChatResponse.ChatRoomDto createChatRoom(Long userId) {
        User user = findUserById(userId);

        // 채팅방 생성
        ChatRoom chatRoom = ChatConverter.toChatRoomEntity(user);
        chatRoomRepository.save(chatRoom);

        // 첫번째 채팅 생성 - "안녕하세요! {nickName}님! {nickName}님의 경험이 궁금해요. {nickName}님의 경험을 들려주세요!"
        Chat firstChat = createFirstChat(user, chatRoom);

        return ChatConverter.toChatRoomDto(chatRoom, firstChat);
    }

    /*
     * user의 채팅방에 채팅을 생성하고 생성된 채팅 정보와 AI 답변 반환
     * @param chatRoomId
     * @param chatDto
     * @return
     */
    @Transactional
    public ChatResponse.ChatsDto createChat(Long userId, Long chatRoomId, ChatRequest.ChatDto chatDto) {
        User user = findUserById(userId);
        ChatRoom chatRoom = findChatRoomById(chatRoomId, user);

        // 사용자 채팅 생성
        Chat chat = ChatConverter.toChatEntity(1, chatDto.getContent(), chatRoom);
        chatRepository.save(chat);

        // 가이드이면 가이드 채팅 생성
        if(chatDto.isGuide()) {
            checkGuideChat(chatRoom);
            return generateGuideChats(chatRoom);
        }

        // AI 답변 생성
        String aiAnswer = createChatAiAnswer(chatRoom, chatDto.getContent());
        Chat aiChat = chatRepository.save(ChatConverter.toChatEntity(0, aiAnswer, chatRoom));

        return ChatConverter.toChatsDto(List.of(aiChat));
    }

    /*
     * user의 채팅방의 채팅 목록을 반환
     * @param userId
     * @param chatRoomId
     * @return chatListDto
     */
    public ChatResponse.ChatListDto getChatList(Long userId, Long chatRoomId) {
        User user = findUserById(userId);
        ChatRoom chatRoom = findChatRoomById(chatRoomId, user);
        List<Chat> chatList = chatRepository.findByChatRoomOrderByChatId(chatRoom);

        return ChatConverter.toChatListDto(chatList);
    }

    /*
     * user의 채팅방을 삭제
     * @param userId
     * @param chatRoomId
     */
    @Transactional
    public void deleteChatRoom(Long userId, Long chatRoomId) {
        User user = findUserById(userId);
        ChatRoom chatRoom = findChatRoomById(chatRoomId, user);

        // 임시 저장된 ChatRoom 인지 확인 후 삭제
        checkTmpChat(user, chatRoom);
        chatRepository.deleteByChatRoomId(chatRoomId);
        chatRoomRepository.delete(chatRoom);
    }

    /*
     * user의 채팅의 요약 정보를 반환
     * @param userId
     * @param chatRoomId
     * @return chatSummaryDto
     */
    public ChatResponse.ChatSummaryDto getChatSummary(Long userId, Long chatRoomId) {
        User user = findUserById(userId);
        ChatRoom chatRoom = findChatRoomById(chatRoomId, user);
        List<Chat> chatList = chatRepository.findByChatRoomOrderByChatId(chatRoom);

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
        User user = findUserById(userId);
        if(user.getTmpChat() == null) {
            return ChatConverter.toNotExistingChatTmpDto();
        }
        // 임시 채팅 제거 후 반환
        Long chatRoomId = user.getTmpChat();
        user.deleteTmpChat();
        return ChatConverter.toExistingChatTmpDto(chatRoomId);
    }

    /*
     * user의 채팅방을 임시 저장
     * @param userId
     * @param chatRoomId
     */
    @Transactional
    public void saveChatTmp(Long userId, Long chatRoomId) {
        User user = findUserById(userId);
        ChatRoom chatRoom = findChatRoomById(chatRoomId, user);

        // 이미 임시 저장된 채팅방이 있는 경우
        if(user.getTmpChat() != null) {
            throw new ChatException(ChatErrorStatus.TMP_CHAT_EXIST);
        }
        user.updateTmpChat(chatRoom.getChatRoomId());
    }

    private static void checkGuideChat(ChatRoom chatRoom) {
        if(chatRoom.getChatList().size() > 2) {
            throw new ChatException(ChatErrorStatus.INVALID_GUIDE_CHAT);
        }
    }

    private ChatResponse.ChatsDto generateGuideChats(ChatRoom chatRoom) {
        Chat guideChat1 = ChatConverter.toChatEntity(0, "걱정 마세요!\n저와 대화하다 보면 경험이 정리될 거예요\uD83D\uDCDD", chatRoom);
        Chat guideChat2 = ChatConverter.toChatEntity(0, "오늘은 어떤 경험을 했나요?\n상황과 해결한 문제를 말해주세요!", chatRoom);
        chatRepository.save(guideChat1);
        chatRepository.save(guideChat2);
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
        if(chatList.size() <= 1) {
            throw new ChatException(ChatErrorStatus.NO_RECORD);
        }
    }

    private ChatSummaryAiResponse generateChatSummary(List<Chat> chatList) {
        ClovaRequest clovaRequest = ClovaRequest.createChatSummaryRequest(chatList);
        String response = clovaService.generateAiResponse(clovaRequest);
        log.info("AI 응답: {}", response);
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
        if(user.getTmpChat() == null) {
            return;
        }
        if(user.getTmpChat().equals(chatRoom.getChatRoomId())) {
            user.deleteTmpChat();
        }
    }

    private String createChatAiAnswer(ChatRoom chatRoom, String userInput) {
        List<Chat> chatHistory = chatRepository.findByChatRoomOrderByChatId(chatRoom);
        ClovaRequest clovaRequest = ClovaRequest.createChatRequest(chatHistory, userInput);
        return clovaService.generateAiResponse(clovaRequest);
    }

    private ChatRoom findChatRoomById(Long chatRoomId, User user) {
        return chatRoomRepository.findByChatRoomIdAndUser(chatRoomId, user)
                .orElseThrow(() -> new ChatException(ChatErrorStatus.CHAT_ROOM_NOT_FOUND));
    }

    private Chat createFirstChat(User user, ChatRoom chatRoom) {
        String nickName = user.getNickName();
        String firstChatContent = String.format("안녕하세요! %s님\n오늘은 어떤 경험을 했나요?\n저와 함께 정리해보아요!", nickName);
        Chat chat = ChatConverter.toChatEntity(0, firstChatContent, chatRoom);
        chatRepository.save(chat);
        return chat;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.UNAUTHORIZED));
    }

}
