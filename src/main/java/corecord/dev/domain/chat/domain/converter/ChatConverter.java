package corecord.dev.domain.chat.domain.converter;

import corecord.dev.domain.chat.domain.dto.response.ChatSummaryAiResponse;
import corecord.dev.domain.chat.domain.dto.response.ChatResponse;
import corecord.dev.domain.chat.domain.entity.Chat;
import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.user.domain.entity.User;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatConverter {

    public static ChatResponse.ChatRoomDto toChatRoomDto(ChatRoom chatRoom, Chat firstChat) {
        return ChatResponse.ChatRoomDto.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .firstChat(firstChat.getContent())
                .build();
    }

    public static ChatRoom toChatRoomEntity(User user) {
        return ChatRoom.builder()
                .user(user)
                .build();
    }

    public static Chat toChatEntity(Integer author, String content, ChatRoom chatRoom) {
        return Chat.builder()
                .author(author)
                .content(content)
                .chatRoom(chatRoom)
                .build();
    }

    public static ChatResponse.ChatDto toChatDto(Chat chat) {
        return ChatResponse.ChatDto.builder()
                .chatId(chat.getChatId())
                .content(chat.getContent())
                .build();
    }

    public static ChatResponse.ChatsDto toChatsDto(List<Chat> chats) {
        return ChatResponse.ChatsDto.builder()
                .chats(chats.stream().map(ChatConverter::toChatDto).toList())
                .build();
    }

    public static ChatResponse.ChatDetailDto toChatDetailDto(Chat chat) {
        return ChatResponse.ChatDetailDto.builder()
                .chatId(chat.getChatId())
                .author(chat.getAuthor() == 0 ? "ai" : "user")
                .content(chat.getContent())
                .created_at(chat.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    public static ChatResponse.ChatListDto toChatListDto(List<Chat> chatList) {
        return ChatResponse.ChatListDto.builder()
                .chats(chatList.stream().map(ChatConverter::toChatDetailDto).toList())
                .build();
    }

    public static ChatResponse.ChatSummaryDto toChatSummaryDto(ChatRoom chatRoom, ChatSummaryAiResponse response) {
        return ChatResponse.ChatSummaryDto.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .title(response.getTitle())
                .content(response.getContent())
                .build();
    }

    public static ChatResponse.ChatTmpDto toExistingChatTmpDto(Long chatRoomId) {
        return ChatResponse.ChatTmpDto.builder()
                .isExist(true)
                .chatRoomId(chatRoomId)
                .build();
    }

    public static ChatResponse.ChatTmpDto toNotExistingChatTmpDto() {
        return ChatResponse.ChatTmpDto.builder()
                .isExist(false)
                .chatRoomId(null)
                .build();
    }
}
