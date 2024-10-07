package corecord.dev.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.common.status.SuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"code", "result", "message", "data"})
public class ApiResponse<T> {
    private final int statusCode;
    private final String result;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    public static <T>ApiResponse<T> SuccessResponse(SuccessStatus status, T data){
        return new ApiResponse<>(status.getCode(), "SUCCESS", status.getMessage(), data);
    }
    public static ApiResponse SuccessResponse(SuccessStatus status){
        return new ApiResponse<>(status.getCode(), "SUCCESS", status.getMessage(), null);
    }

    public static ApiResponse FailureResponse(int statusCode, String message){
        return new ApiResponse<>(statusCode, "FAILURE", message, null);
    }

    public static ApiResponse FailureResponse(ErrorStatus errorStatus){
        return new ApiResponse<>(errorStatus.getCode(), "FAILURE", errorStatus.getMessage(), null);
    }
}
