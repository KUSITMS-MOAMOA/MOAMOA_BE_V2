package corecord.dev.domain.analysis.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

public class AnalysisRequest {

    @Data
    public static class AnalysisUpdateDto {
        @NotNull(message = "역량 분석 id를 입력해주세요.")
        private Long analysisId;
        private String title;
        private String content;
        private Map<String, String> abilityMap;
    }
}
