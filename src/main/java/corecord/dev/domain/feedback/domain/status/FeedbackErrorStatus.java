package corecord.dev.domain.feedback.domain.status;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FeedbackErrorStatus implements BaseErrorStatus {
    OVERFLOW_FEEDBACK_COMMENT(HttpStatus.BAD_REQUEST, "E203_OVERFLOW_FEEDBACK_COMMENT", "피드백 내용은 200자 이내여야 합니다."),
    UNAUTHORIZED_FEEDBACK(HttpStatus.UNAUTHORIZED, "E203_FEEDBACK_UNAUTHORIZED", "유저가 해당 피드백에 대한 권한이 없습니다."),
    ALREADY_FEEDBACK(HttpStatus.BAD_REQUEST, "E203_ALREADY_FEEDBACK", "이미 피드백을 작성한 경험입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
