package corecord.dev.common.exception;

import corecord.dev.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class GeneralExceptionAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = { GeneralException.class })
    protected ResponseEntity<ApiResponse<String>> handleException(GeneralException e) {
        log.error("Handling GeneralException: {}", e.getMessage());

        ApiResponse<String> response = ApiResponse.FailureResponse(e.getErrorStatus());
        HttpStatus status = e.getErrorStatus() != null ? e.getErrorStatus().getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;

        return new ResponseEntity<>(response, status);
    }
}
