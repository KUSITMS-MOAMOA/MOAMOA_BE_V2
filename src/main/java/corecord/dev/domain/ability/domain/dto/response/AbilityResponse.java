package corecord.dev.domain.ability.domain.dto.response;

import corecord.dev.domain.ability.domain.enums.Keyword;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

public class AbilityResponse {

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
    public static class KeywordListDto {
        private List<String> keywordList;
    }

    @Data
    public static class KeywordStateDto {
        private String keyword;
        private Long count;
        private Long percent;

        public KeywordStateDto(Keyword keyword, Long count, Double percent) {
            this.keyword = keyword.getValue();
            this.count = count;
            this.percent = Math.round(percent);
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
