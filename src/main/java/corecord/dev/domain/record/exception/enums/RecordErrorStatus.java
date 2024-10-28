package corecord.dev.domain.record.exception.enums;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecordErrorStatus implements BaseErrorStatus {
    OVERFLOW_MEMO_RECORD_TITLE(HttpStatus.BAD_REQUEST, "E0400_OVERFLOW_TITLE", "메모 제목은 15자 이내여야 합니다."),
    OVERFLOW_MEMO_RECORD_CONTENT(HttpStatus.BAD_REQUEST, "E0400_OVERFLOW_CONTENT", "메모 내용은 500자 이내여야 합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
