package corecord.dev.domain.user.exception.enums;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorStatus implements BaseErrorStatus {
    INVALID_USER_NICKNAME(HttpStatus.BAD_REQUEST, "E101_NICKNAME", "유효하지 않은 닉네임입니다."),
    INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "E101_STATUS", "신분상태의 입력이 잘못되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
