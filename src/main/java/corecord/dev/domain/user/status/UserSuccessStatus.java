package corecord.dev.domain.user.status;

import corecord.dev.common.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserSuccessStatus implements BaseSuccessStatus {

    USER_REGISTER_SUCCESS(HttpStatus.CREATED, "S101", "회원가입이 성공적으로 완료되었습니다."),
    USER_LOGOUT_SUCCESS(HttpStatus.OK, "S802", "로그아웃이 성공적으로 완료되었습니다."),
    USER_DELETE_SUCCESS(HttpStatus.OK, "S804", "회원탈퇴가 성공적으로 완료되었습니다."),
    USER_UPDATE_SUCCESS(HttpStatus.OK, "S803", "회원정보 수정이 성공적으로 완료되었습니다."),
    GET_USER_INFO_SUCCESS(HttpStatus.OK, "S801", "회원정보 조회가 성공적으로 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
