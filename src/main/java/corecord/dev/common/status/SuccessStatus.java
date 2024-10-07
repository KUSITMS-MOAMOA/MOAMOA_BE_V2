package corecord.dev.common.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus {

    SUCCESS(HttpStatus.OK, 200, "응답에 성공했습니다.");


    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
