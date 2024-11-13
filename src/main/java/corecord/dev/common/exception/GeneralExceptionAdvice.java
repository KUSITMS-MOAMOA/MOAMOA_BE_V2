package corecord.dev.common.exception;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.domain.ability.exception.model.AbilityException;
import corecord.dev.domain.analysis.exception.model.AnalysisException;
import corecord.dev.domain.auth.exception.model.TokenException;
import corecord.dev.domain.chat.exception.model.ChatException;
import corecord.dev.domain.folder.exception.model.FolderException;
import corecord.dev.domain.record.exception.model.RecordException;
import corecord.dev.domain.user.exception.model.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
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

    // AnalysisException 처리
    @ExceptionHandler(AnalysisException.class)
    public ResponseEntity<ApiResponse<Void>> handleAnalysisException(AnalysisException e) {
        log.warn(">>>>>>>>AnalysisException: {}", e.getAnalysisErrorStatus().getMessage());
        return ApiResponse.error(e.getAnalysisErrorStatus());
    }

    // AbilityException 처리
    @ExceptionHandler(AbilityException.class)
    public ResponseEntity<ApiResponse<Void>> handleAbilityException(AbilityException e) {
        log.warn(">>>>>>>>AbilityException: {}", e.getAbilityErrorStatus().getMessage());
        return ApiResponse.error(e.getAbilityErrorStatus());
    }

    // ChatException 처리
    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ApiResponse<Void>> handleChatException(ChatException e) {
        log.warn(">>>>>>>>ChatException: {}", e.getChatErrorStatus().getMessage());
        return ApiResponse.error(e.getChatErrorStatus());
    }

    // GeneralException 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e) {
        log.warn(">>>>>>>>GeneralException: {}", e.getErrorStatus().getMessage());
        return ApiResponse.error(e.getErrorStatus());
    }

    // HttpRequestMethodNotSupportedException 처리 (지원하지 않는 HTTP 메소드 요청이 들어온 경우)
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                         HttpHeaders headers,
                                                                         HttpStatusCode status,
                                                                         WebRequest request) {
        String errorMessage = "지원하지 않는 HTTP 메소드 요청입니다: " + ex.getMethod();
        logError("HttpRequestMethodNotSupportedException", errorMessage);
        return ApiResponse.error(ErrorStatus.METHOD_NOT_ALLOWED, errorMessage);
    }

    // MissingServletRequestParameterException 처리 (필수 쿼리 파라미터가 입력되지 않은 경우)
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          WebRequest request) {
        String errorMessage = "필수 파라미터 '" + ex.getParameterName() + "'가 없습니다.";
        logError("MissingServletRequestParameterException", errorMessage);
        return ApiResponse.error(ErrorStatus.BAD_REQUEST, errorMessage);
    }

    // MethodArgumentNotValidException 처리 (RequestBody로 들어온 필드들의 유효성 검증에 실패한 경우)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String combinedErrors = extractFieldErrors(ex.getBindingResult().getFieldErrors());
        logError("Validation error", combinedErrors);
        return ApiResponse.error(ErrorStatus.BAD_REQUEST, combinedErrors);
    }

    // NullPointerException 처리
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Object> handleNullPointerException(NullPointerException e) {
        String errorMessage = "서버에서 예기치 않은 오류가 발생했습니다. 요청을 처리하는 중에 Null 값이 참조되었습니다.";
        logError("NullPointerException", e);
        return ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }

    // IllegalArgumentException 처리 (잘못된 인자가 전달된 경우)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        String errorMessage = "잘못된 요청입니다: " + e.getMessage();
        logError("IllegalArgumentException", errorMessage);
        return ApiResponse.error(ErrorStatus.BAD_REQUEST, errorMessage);
    }

    // MissingRequestHeaderException 처리 (필수 헤더가 누락된 경우)
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Object> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        String errorMessage = "필수 헤더 '" + ex.getHeaderName() + "'가 없습니다.";
        logError("MissingRequestHeaderException", errorMessage);
        return ApiResponse.error(ErrorStatus.BAD_REQUEST, errorMessage);
    }

    // Security 인증 관련 처리
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handleSecurityException(SecurityException e) {
        logError(e.getMessage(), e);
        return ApiResponse.error(ErrorStatus.UNAUTHORIZED);
    }

    // 기타 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error(">>>>>>>>Internal Server Error: {}", e.getMessage());
        e.printStackTrace();
        return ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
    }

    // 로그 기록 메서드
    private void logError(String message, Object errorDetails) {
        log.error("{}: {}", message, errorDetails);
    }

    // 유효성 검증 오류 메시지 추출 메서드 (FieldErrors)
    private String extractFieldErrors(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
    }
}