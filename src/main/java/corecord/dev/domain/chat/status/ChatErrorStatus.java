package corecord.dev.domain.chat.status;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ChatErrorStatus implements BaseErrorStatus {
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "E0302_CHAT_ROOM_NOT_FOUND", "존재하지 않는 채팅방입니다."),
    INVALID_GUIDE_CHAT(HttpStatus.BAD_REQUEST, "E0302_INVALID_GUIDE_CHAT", "가이드 채팅은 처음에만 할 수 있습니다."),
    OVERFLOW_SUMMARY_TITLE(HttpStatus.BAD_REQUEST, "E0305_OVERFLOW_SUMMARY_TITLE", "경험 제목은 30자 이내여야 합니다."),
    OVERFLOW_SUMMARY_CONTENT(HttpStatus.BAD_REQUEST, "E0305_OVERFLOW_SUMMARY_CONTENT", "경험 요약 내용은 500자 이내여야 합니다."),
    INVALID_CHAT_SUMMARY(HttpStatus.BAD_REQUEST, "E0305_INVALID_CHAT_SUMMARY", "채팅 경험 요약 파싱 중 오류가 발생했습니다."),
    NO_RECORD(HttpStatus.BAD_REQUEST, "E0305_NO_RECORD", "경험 기록의 내용이 충분하지 않습니다."),
    TMP_CHAT_EXIST(HttpStatus.BAD_REQUEST, "E0307_TMP_CHAT_EXIST", "임시 채팅이 이미 존재합니다."),;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
