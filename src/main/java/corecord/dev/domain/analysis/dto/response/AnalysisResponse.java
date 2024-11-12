package corecord.dev.domain.analysis.dto.response;

import corecord.dev.domain.ability.dto.response.AbilityResponse;
import corecord.dev.domain.record.constant.RecordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

public class AnalysisResponse {

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class AnalysisDto {
        private Long analysisId;
        private Long chatRoomId;
        private Long recordId;
        private RecordType recordType;
        private String recordTitle;
        private String recordContent;
        private List<AbilityResponse.AbilityDto> abilityDtoList;
        private String comment;
        private String createdAt;
    }

}
