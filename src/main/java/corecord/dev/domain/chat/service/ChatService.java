package corecord.dev.domain.chat.service;

import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.domain.chat.converter.ChatConverter;
import corecord.dev.domain.chat.dto.request.ChatRequest;
import corecord.dev.domain.chat.dto.request.ClovaRequest;
import corecord.dev.domain.chat.dto.response.ChatResponse;
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
    public ChatResponse.ChatDto createChat(Long userId, Long chatRoomId, ChatRequest.ChatDto chatDto) {
        User user = findUserById(userId);
        ChatRoom chatRoom = findChatRoomById(chatRoomId, user);

        // 사용자 채팅 생성
        Chat chat = ChatConverter.toChatEntity(1, chatDto.getContent(), chatRoom);
        chatRepository.save(chat);

        // AI 답변 생성
        String aiAnswer = createAiAnswer(chatRoom, chatDto.getContent());
        Chat aiChat = chatRepository.save(ChatConverter.toChatEntity(0, aiAnswer, chatRoom));

        return ChatConverter.toChatDto(aiChat);
    }

    private String createAiAnswer(ChatRoom chatRoom, String userInput) {
        List<Chat> chatHistory = chatRepository.findByChatRoomOrderByChatId(chatRoom);
        ClovaRequest clovaRequest = ClovaRequest.createRequest(chatHistory, userInput);
        return clovaService.generateAiResponse(clovaRequest);
    }

    private ChatRoom findChatRoomById(Long chatRoomId, User user) {
        return chatRoomRepository.findByChatRoomIdAndUser(chatRoomId, user)
                .orElseThrow(() -> new ChatException(ChatErrorStatus.CHAT_ROOM_NOT_FOUND));
    }

    private Chat createFirstChat(User user, ChatRoom chatRoom) {
        String nickName = user.getNickName();
        String firstChatContent = String.format("안녕하세요! %s님! %s님의 경험이 궁금해요. %s님의 경험을 들려주세요!", nickName, nickName, nickName);
        Chat chat = ChatConverter.toChatEntity(1, firstChatContent, chatRoom);
        chatRepository.save(chat);
        return chat;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.UNAUTHORIZED));
    }

}
