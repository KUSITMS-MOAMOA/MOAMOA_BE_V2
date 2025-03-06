package corecord.dev.domain.chat.application;

import corecord.dev.domain.chat.domain.dto.request.ChatRequest;
import corecord.dev.domain.chat.domain.dto.response.ChatResponse;
import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.user.domain.entity.User;

public interface ChatService {
    ChatResponse.ChatRoomDto createChatRoom(Long userId);
    ChatResponse.ChatsDto createChat(Long userId, Long chatRoomId, ChatRequest.ChatDto chatDto);
    ChatResponse.ChatListDto getChatList(Long userId, Long chatRoomId);
    void deleteChatRoom(Long userId, Long chatRoomId);

    ChatResponse.ChatSummaryDto getChatSummary(Long userId, Long chatRoomId);
    ChatResponse.ChatTmpDto getChatTmp(Long userId);
    void saveChatTmp(Long userId, Long chatRoomId);

    ChatRoom createExampleChat(User user);

}
