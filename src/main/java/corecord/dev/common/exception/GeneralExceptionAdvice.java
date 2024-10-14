package corecord.dev.common.exception;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.status.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class GeneralExceptionAdvice extends ResponseEntityExceptionHandler {

    // GeneralException 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e) {
        log.warn(">>>>>>>>GeneralException: {}", e.getErrorStatus().getMessage());
        return ApiResponse.error(e.getErrorStatus());
    }

    // 기타 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error(">>>>>>>>Internal Server Error: {}", e.getMessage());
        e.printStackTrace();
        return ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
    }
}
