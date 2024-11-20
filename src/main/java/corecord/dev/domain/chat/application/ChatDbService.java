package corecord.dev.domain.chat.application;

import corecord.dev.domain.chat.domain.converter.ChatConverter;
import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.chat.domain.repository.ChatRepository;
import corecord.dev.domain.chat.domain.repository.ChatRoomRepository;
import corecord.dev.domain.user.domain.entity.User;
import corecord.dev.domain.user.domain.repository.UserRepository;
import corecord.dev.domain.chat.exception.ChatException;
import corecord.dev.domain.chat.status.ChatErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatDbService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoom createChatRoom(User user) {
        ChatRoom chatRoom = ChatConverter.toChatRoomEntity(user);
        return chatRoomRepository.save(chatRoom);
    }

    @Transactional
    public Chat saveChat(int author, String content, ChatRoom chatRoom) {
        Chat chat = ChatConverter.toChatEntity(author, content, chatRoom);
        return chatRepository.save(chat);
    }

    public ChatRoom findChatRoomById(Long chatRoomId, User user) {
        return chatRoomRepository.findByChatRoomIdAndUser(chatRoomId, user)
                .orElseThrow(() -> new ChatException(ChatErrorStatus.CHAT_ROOM_NOT_FOUND));
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ChatException(ChatErrorStatus.CHAT_ROOM_NOT_FOUND));
    }

    @Transactional
    public void deleteChatRoom(ChatRoom chatRoom) {
        chatRepository.deleteByChatRoomId(chatRoom.getChatRoomId());
        chatRoomRepository.delete(chatRoom);
    }

    public List<Chat> findChatsByChatRoom(ChatRoom chatRoom) {
        return chatRepository.findByChatRoomOrderByChatId(chatRoom);
    }

    @Transactional
    public void updateUserTmpChat(User user, Long chatRoomId) {
        user.updateTmpChat(chatRoomId);
    }

    @Transactional
    public void deleteUserTmpChat(User user) {
        user.deleteTmpChat();
    }
}
