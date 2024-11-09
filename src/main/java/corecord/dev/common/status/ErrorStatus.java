package corecord.dev.common.status;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorStatus {

    /**
     *  Error Code
     *  400 : 잘못된 요청
     *  401 : JWT에 대한 오류
     *  403 : 요청한 정보에 대한 권한 없음.
     *  404 : 존재하지 않는 정보에 대한 요청.
     */

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "E0400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E0401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "E0403", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "E0404", "요청한 자원을 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "E0405", "허용되지 않은 메소드입니다."),
    AI_RESPONSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E0500_CHAT_AI_RESPONSE_ERROR", "AI 응답 생성 중 오류가 발생했습니다."),
    AI_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "E0400_AI_CLIENT_ERROR", "AI 클라이언트 요청 오류가 발생했습니다."),
    AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E0500_CHAT_SERVER_ERROR", "AI 서버에 오류가 발생했습니다."),


    /**
     *  Error Code
     *  500 : 서버 내부 오류
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E9999", "서버 내부 오류입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
