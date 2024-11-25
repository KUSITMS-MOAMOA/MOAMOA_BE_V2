package corecord.dev.domain.record.status;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecordErrorStatus implements BaseErrorStatus {
    OVERFLOW_MEMO_RECORD_TITLE(HttpStatus.BAD_REQUEST, "E0400_OVERFLOW_TITLE", "메모 제목은 50자 이내여야 합니다."),
    OVERFLOW_MEMO_RECORD_CONTENT(HttpStatus.BAD_REQUEST, "E0400_OVERFLOW_CONTENT", "메모 내용은 500자 이내여야 합니다."),
    NOT_ENOUGH_MEMO_RECORD_CONTENT(HttpStatus.BAD_REQUEST, "E0400_NOT_ENOUGH_CONTENT", "메모 내용은 30자 이상이어야 합니다."),
    USER_RECORD_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E401_RECORD_UNAUTHORIZED", "유저가 경험 기록에 대한 권한이 없습니다."),
    RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "E0404_RECORD", "존재하지 않는 경험 기록입니다."),
    ALREADY_TMP_MEMO(HttpStatus.BAD_REQUEST, "E0400_TMP_MEMO", "유저가 이미 임시 저장된 메모를 가지고 있습니다."),
    NO_RECORD(HttpStatus.BAD_REQUEST, "E0400_NO_RECORD", "경험 기록의 내용이 충분하지 않습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
