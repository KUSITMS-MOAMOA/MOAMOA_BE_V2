package corecord.dev.domain.ability.status;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AbilityErrorStatus implements BaseErrorStatus {
    INVALID_KEYWORD(HttpStatus.BAD_REQUEST, "E400_INVALID_KEYWORD", "역량 분석에 존재하지 않는 키워드입니다."),
    INVALID_ABILITY_KEYWORD(HttpStatus.INTERNAL_SERVER_ERROR, "E500_INVALID_ABILITY_KEYWORD", "역량 키워드 파싱 중 오류가 발생했습니다."),
    ;



    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
