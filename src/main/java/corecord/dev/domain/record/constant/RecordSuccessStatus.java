package corecord.dev.domain.record.constant;

import corecord.dev.common.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecordSuccessStatus implements BaseSuccessStatus {

    MEMO_RECORD_CREATE_SUCCESS(HttpStatus.CREATED, "S307", "메모 경험 기록이 성공적으로 완료되었습니다."),
    MEMO_RECORD_DETAIL_GET_SUCCESS(HttpStatus.OK, "S401", "메모 경험 기록 세부 조회가 성공적으로 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
