package corecord.dev.domain.ability.status;

import corecord.dev.common.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AbilitySuccessStatus implements BaseSuccessStatus {
    KEYWORD_LIST_GET_SUCCESS(HttpStatus.OK, "S505", "역량 키워드 리스트 조회가 성공적으로 완료되었습니다."),
    KEYWORD_GRAPH_GET_SUCCESS(HttpStatus.OK, "S202", "역량 키워드 그래프 조회가 성공적으로 완료되었습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
