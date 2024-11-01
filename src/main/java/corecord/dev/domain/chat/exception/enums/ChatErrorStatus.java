package corecord.dev.domain.chat.exception.enums;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ChatErrorStatus implements BaseErrorStatus {
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "E0302_CHAT_ROOM_NOT_FOUND", "존재하지 않는 채팅방입니다."),
    CHAT_AI_RESPONSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E0302_CHAT_AI_RESPONSE_ERROR", "AI 응답 생성 중 오류가 발생했습니다."),
    CHAT_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "E0302_CHAT_CLIENT_ERROR", "AI 클라이언트 요청 오류가 발생했습니다."),
    CHAT_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E0302_CHAT_SERVER_ERROR", "AI 서버에 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
