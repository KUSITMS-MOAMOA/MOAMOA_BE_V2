package corecord.dev.domain.chat.exception.enums;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ChatErrorStatus implements BaseErrorStatus {
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "E0302_CHAT_ROOM_NOT_FOUND", "존재하지 않는 채팅방입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
