package corecord.dev.domain.analysis.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

public class AnalysisRequest {

    @Data @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnalysisUpdateDto {
        @NotNull(message = "역량 분석 id를 입력해주세요.")
        private Long analysisId;
        private String title;
        private String content;
        private Map<String, String> abilityMap;
    }
}
