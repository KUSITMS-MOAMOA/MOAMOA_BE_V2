package corecord.dev.domain.chat.controller;

import com.nimbusds.oauth2.sdk.SuccessResponse;
import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.web.UserId;
import corecord.dev.domain.chat.constant.ChatSuccessStatus;
import corecord.dev.domain.chat.dto.request.ChatRequest;
import corecord.dev.domain.chat.dto.response.ChatResponse;
import corecord.dev.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static corecord.dev.domain.chat.constant.ChatSuccessStatus.CHAT_ROOM_CREATE_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records/chat")
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse.ChatRoomDto>> createChatRoom(
            @UserId Long userId
    ) {
        ChatResponse.ChatRoomDto chatRoomDto = chatService.createChatRoom(userId);
        return ApiResponse.success(CHAT_ROOM_CREATE_SUCCESS, chatRoomDto);
    }

    @PostMapping("/{chatRoomId}")
    public ResponseEntity<ApiResponse<ChatResponse.ChatDto>> createChat(
            @UserId Long userId,
            @PathVariable(name = "chatRoomId") Long chatRoomId,
            @RequestBody ChatRequest.ChatDto chatDto
    ) {
        ChatResponse.ChatDto chatResponse = chatService.createChat(userId, chatRoomId, chatDto);
        return ApiResponse.success(ChatSuccessStatus.CHAT_CREATE_SUCCESS, chatResponse);
    }

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ApiResponse<ChatResponse.ChatListDto>> getChatList(
            @UserId Long userId,
            @PathVariable(name = "chatRoomId") Long chatRoomId
    ) {
        ChatResponse.ChatListDto chatListDto = chatService.getChatList(userId, chatRoomId);
        return ApiResponse.success(ChatSuccessStatus.GET_CHAT_SUCCESS, chatListDto);
    }

    @DeleteMapping("/{chatRoomId}")
    public ResponseEntity<ApiResponse<Void>> deleteChatRoom(
            @UserId Long userId,
            @PathVariable(name = "chatRoomId") Long chatRoomId
    ) {
        chatService.deleteChatRoom(userId, chatRoomId);
        return ApiResponse.success(ChatSuccessStatus.CHAT_DELETE_SUCCESS);
    }

    @GetMapping("/{chatRoomId}/summary")
    public ResponseEntity<ApiResponse<ChatResponse.ChatSummaryDto>> getChatSummary(
            @UserId Long userId,
            @PathVariable(name = "chatRoomId") Long chatRoomId
    ) {
        ChatResponse.ChatSummaryDto chatSummaryDto = chatService.getChatSummary(userId, chatRoomId);
        return ApiResponse.success(ChatSuccessStatus.GET_CHAT_SUMMARY_SUCCESS, chatSummaryDto);
    }

    @GetMapping("/tmp")
    public ResponseEntity<ApiResponse<ChatResponse.ChatTmpDto>> getChatTmp(
            @UserId Long userId
    ) {
        ChatResponse.ChatTmpDto chatTmpDto = chatService.getChatTmp(userId);
        return ApiResponse.success(ChatSuccessStatus.GET_CHAT_TMP_SUCCESS, chatTmpDto);
    }
}
