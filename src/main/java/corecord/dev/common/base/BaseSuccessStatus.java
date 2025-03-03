package corecord.dev.common.base;

import org.springframework.http.HttpStatus;

public interface BaseSuccessStatus {
    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}
