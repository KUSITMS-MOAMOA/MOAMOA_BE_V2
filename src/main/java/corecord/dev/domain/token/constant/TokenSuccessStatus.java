package corecord.dev.domain.token.constant;

import corecord.dev.common.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TokenSuccessStatus implements BaseSuccessStatus {
    ISSUE_TOKENS_SUCCESS(HttpStatus.OK, "S104", "Access Token 및 Refresh Token 발급 성공입니다."),
    REISSUE_ACCESS_TOKEN_SUCCESS(HttpStatus.CREATED, "S103", "Access Token 재발급 성공입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
