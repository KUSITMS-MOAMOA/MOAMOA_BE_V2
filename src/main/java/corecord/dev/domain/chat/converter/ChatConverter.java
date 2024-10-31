package corecord.dev.domain.chat.converter;

import corecord.dev.domain.chat.dto.response.ChatResponse;
import corecord.dev.domain.chat.entity.Chat;
import corecord.dev.domain.chat.entity.ChatRoom;
import corecord.dev.domain.user.entity.User;

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
}
