package corecord.dev.domain.token.exception.enums;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TokenErrorStatus implements BaseErrorStatus {
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "E001", "유효하지 않은 액세스 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "E002", "유효하지 않은 리프레쉬 토큰입니다."),
    INVALID_REGISTER_TOKEN(HttpStatus.UNAUTHORIZED, "E003", "유효하지 않은 회원가입 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "E004", "해당 유저 ID의 리프레쉬 토큰이 없습니다."),
    REGISTER_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "E005", "회원가입 토큰이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
