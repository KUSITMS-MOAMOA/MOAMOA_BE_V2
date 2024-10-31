package corecord.dev.domain.analysis.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

public class AnalysisResponse {

    @Data
    @Builder
    @Getter
    @AllArgsConstructor
    public static class AbilityDto {
        private String keyword;
        private String content;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class AnalysisDto {
        private Long analysisId;
        private Long recordId;
        private String recordTitle;
        private String recordContent;
        private List<AbilityDto> abilityDtoList;
        private String comment;
        private String createdAt;
    }
}
