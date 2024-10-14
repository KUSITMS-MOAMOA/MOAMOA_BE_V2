package corecord.dev.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.common.status.SuccessStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@Getter
@RequiredArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "data"})
public class ApiResponse<T> {
    @JsonProperty("is_success")
    private final Boolean isSuccess;  // 성공 여부
    private final String code;        // 사용자 정의 코드 (e.g., S001, E999)
    private final String message;     // 응답 메시지
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;             // 응답 데이터

    // 성공 응답 (데이터 없음)
    public static <T> ResponseEntity<ApiResponse<T>> success(SuccessStatus successStatus) {
        ApiResponse<T> response = new ApiResponse<>(true, successStatus.getCode(), successStatus.getMessage(), null);
        return ResponseEntity.status(successStatus.getHttpStatus()).body(response);
    }

    // 성공 응답 (데이터 있음)
    public static <T> ResponseEntity<ApiResponse<T>> success(SuccessStatus successStatus, T data) {
        ApiResponse<T> response = new ApiResponse<>(true, successStatus.getCode(), successStatus.getMessage(), data);
        return ResponseEntity.status(successStatus.getHttpStatus()).body(response);
    }

    // 에러 응답 (데이터 없음)
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorStatus errorStatus) {
        ApiResponse<T> response = new ApiResponse<>(false, errorStatus.getCode(), errorStatus.getMessage(), null);
        return ResponseEntity.status(errorStatus.getHttpStatus()).body(response);
    }

    // 에러 응답 (데이터 있음)
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorStatus errorStatus, T data) {
        ApiResponse<T> response = new ApiResponse<>(false, errorStatus.getCode(), errorStatus.getMessage(), data);
        return ResponseEntity.status(errorStatus.getHttpStatus()).body(response);
    }
}
