package corecord.dev.common.exception;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.domain.folder.exception.model.FolderException;
import corecord.dev.domain.record.exception.model.RecordException;
import corecord.dev.domain.token.exception.model.TokenException;
import corecord.dev.domain.user.exception.model.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class GeneralExceptionAdvice extends ResponseEntityExceptionHandler {

    // UserException 처리
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserException(UserException e) {
        log.warn(">>>>>>>>UserException: {}", e.getUserErrorStatus().getMessage());
        return ApiResponse.error(e.getUserErrorStatus());
    }

    // TokenException 처리
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenException(TokenException e) {
        log.warn(">>>>>>>>TokenException: {}", e.getTokenErrorStatus().getMessage());
        return ApiResponse.error(e.getTokenErrorStatus());
    }

    // FolderException 처리
    @ExceptionHandler(FolderException.class)
    public ResponseEntity<ApiResponse<Void>> handleFolderException(FolderException e) {
        log.warn(">>>>>>>>FolderException: {}", e.getFolderErrorStatus().getMessage());
        return ApiResponse.error(e.getFolderErrorStatus());
    }

    // RecordException 처리
    @ExceptionHandler(RecordException.class)
    public ResponseEntity<ApiResponse<Void>> handleRecordException(RecordException e) {
        log.warn(">>>>>>>>RecordException: {}", e.getRecordErrorStatus().getMessage());
        return ApiResponse.error(e.getRecordErrorStatus());
    }

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
