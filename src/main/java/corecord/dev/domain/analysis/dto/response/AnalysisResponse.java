package corecord.dev.domain.analysis.dto.response;

import corecord.dev.domain.analysis.constant.Keyword;
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

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class KeywordListDto {
        private List<String> keywordList;
    }

    @Data
    public static class KeywordStateDto {
        private String keyword;
        private Long count;
        private String percent;

        public KeywordStateDto(Keyword keyword, Long count, Double percent) {
            this.keyword = keyword.getValue();
            this.count = count;
            this.percent = Math.round(percent) + "%";
        }
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class GraphDto {
        List<KeywordStateDto> keywordGraph;
    }
}
