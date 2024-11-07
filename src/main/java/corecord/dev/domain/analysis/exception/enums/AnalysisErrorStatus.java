package corecord.dev.domain.analysis.exception.enums;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AnalysisErrorStatus implements BaseErrorStatus {
    INVALID_KEYWORD(HttpStatus.BAD_REQUEST, "E400_INVALID_KEYWORD", "역량 분석에 존재하지 않는 키워드입니다."),
    OVERFLOW_ANALYSIS_CONTENT(HttpStatus.BAD_REQUEST, "E0400_OVERFLOW_CONTENT", "경험 기록 내용은 500자 이내여야 합니다."),
    OVERFLOW_ANALYSIS_COMMENT(HttpStatus.BAD_REQUEST, "E0400_OVERFLOW_COMMENT", "경험 기록 코멘트는 200자 이내여야 합니다."),
    OVERFLOW_ANALYSIS_KEYWORD_CONTENT(HttpStatus.BAD_REQUEST, "E0400_OVERFLOW_KEYWORD_CONTENT", "경험 기록 키워드별 내용은 200자 이내여야 합니다."),
    USER_ANALYSIS_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E401_ANALYSIS_UNAUTHORIZED", "유저가 역량 분석에 대한 권한이 없습니다."),
    ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "E0404_ANALYSIS", "존재하지 않는 역량 분석입니다."),
    INVALID_ABILITY_ANALYSIS(HttpStatus.BAD_REQUEST, "E400_INVALID_ANALYSIS", "역량 분석 데이터 파싱 중 오류가 발생했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
