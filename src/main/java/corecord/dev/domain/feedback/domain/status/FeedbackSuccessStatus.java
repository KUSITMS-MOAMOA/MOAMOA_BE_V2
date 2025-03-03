package corecord.dev.domain.feedback.domain.status;

import corecord.dev.common.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FeedbackSuccessStatus implements BaseSuccessStatus {
    FEEDBACK_POST_SUCCESS(HttpStatus.CREATED, "S203", "AI 만족도 조사 결과가 성공적으로 저장되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
