package corecord.dev.domain.chat.controller;

import com.nimbusds.oauth2.sdk.SuccessResponse;
import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.web.UserId;
import corecord.dev.domain.chat.dto.response.ChatResponse;
import corecord.dev.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static corecord.dev.domain.chat.constant.ChatSuccessStatus.CHAT_ROOM_CREATE_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse.ChatRoomDto>> createChatRoom(
            @UserId Long userId
    ) {
        ChatResponse.ChatRoomDto chatRoomDto = chatService.createChatRoom(userId);
        return ApiResponse.success(CHAT_ROOM_CREATE_SUCCESS, chatRoomDto);
    }

}
