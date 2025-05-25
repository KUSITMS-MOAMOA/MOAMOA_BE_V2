package corecord.dev.domain.analysis.status;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AnalysisErrorStatus implements BaseErrorStatus {
    OVERFLOW_ANALYSIS_CONTENT(HttpStatus.BAD_REQUEST, "E0400_OVERFLOW_CONTENT", "경험 기록 내용은 1000자 이내여야 합니다."),
    OVERFLOW_ANALYSIS_COMMENT(HttpStatus.INTERNAL_SERVER_ERROR, "E0500_OVERFLOW_COMMENT", "경험 기록 코멘트는 200자 이내여야 합니다."),
    OVERFLOW_ANALYSIS_KEYWORD_CONTENT(HttpStatus.INTERNAL_SERVER_ERROR, "E0500_OVERFLOW_KEYWORD_CONTENT", "경험 기록 키워드별 내용은 200자 이내여야 합니다."),
    USER_ANALYSIS_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E401_ANALYSIS_UNAUTHORIZED", "유저가 역량 분석에 대한 권한이 없습니다."),
    ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "E0404_ANALYSIS", "존재하지 않는 역량 분석입니다."),
    INVALID_ABILITY_ANALYSIS(HttpStatus.INTERNAL_SERVER_ERROR, "E500_INVALID_ANALYSIS", "역량 분석 데이터 파싱 중 오류가 발생했습니다."),
    AI_RESPONSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E0500_ANALYSIS_AI_RESPONSE_ERROR", "AI 응답 생성 중 오류가 발생했습니다."),
    AI_CLIENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E0400_AI_CLIENT_ERROR", "AI 클라이언트 요청 오류가 발생했습니다."),
    AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E0500_ANALYSIS_SERVER_ERROR", "AI 서버에 오류가 발생했습니다."),;
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
