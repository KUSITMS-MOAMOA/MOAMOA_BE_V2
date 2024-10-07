package corecord.dev.common.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus {

    /**
     *  Error Code
     *  400 : 잘못된 요청
     *  401 : JWT에 대한 오류
     *  403 : 요청한 정보에 대한 권한 없음.
     *  404 : 존재하지 않는 정보에 대한 요청.
     */

    BAD_REQUEST(HttpStatus.BAD_REQUEST, 400, "잘못된 요청입니다");


    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
