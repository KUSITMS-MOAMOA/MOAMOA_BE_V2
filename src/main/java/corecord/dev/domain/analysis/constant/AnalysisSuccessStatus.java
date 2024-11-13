package corecord.dev.domain.analysis.constant;

import corecord.dev.common.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AnalysisSuccessStatus implements BaseSuccessStatus {
    ANALYSIS_POST_SUCCESS(HttpStatus.CREATED, "S501", "역량별 경험 분석이 성공적으로 완료되었습니다."),
    ANALYSIS_GET_SUCCESS(HttpStatus.OK, "S502", "역량별 경험 조회가 성공적으로 완료되었습니다."),
    ANALYSIS_UPDATE_SUCCESS(HttpStatus.OK, "S701", "역량별 경험 수정이 성공적으로 완료되었습니다."),
    ANALYSIS_DELETE_SUCCESS(HttpStatus.OK, "S702", "역량별 경험 삭제가 성공적으로 완료되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
