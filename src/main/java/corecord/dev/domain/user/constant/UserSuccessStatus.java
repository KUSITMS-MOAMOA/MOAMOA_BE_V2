package corecord.dev.domain.user.constant;

import corecord.dev.common.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserSuccessStatus implements BaseSuccessStatus {

    USER_REGISTER_SUCCESS(HttpStatus.CREATED, "S101", "회원가입이 성공적으로 완료되었습니다."),
    USER_LOGOUT_SUCCESS(HttpStatus.OK, "S801", "로그아웃이 성공적으로 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
