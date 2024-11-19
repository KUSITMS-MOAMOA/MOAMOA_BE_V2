package corecord.dev.domain.analysis.domain.converter;

import corecord.dev.domain.ability.domain.converter.AbilityConverter;
import corecord.dev.domain.ability.domain.dto.response.AbilityResponse;
import corecord.dev.domain.analysis.domain.dto.response.AnalysisResponse;
import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.record.domain.entity.RecordType;
import corecord.dev.domain.record.domain.entity.Record;

import java.util.List;

public class AnalysisConverter {
    public static Analysis toAnalysis(String content, String comment, Record record) {
        return Analysis.builder()
                .content(content)
                .comment(comment)
                .record(record)
                .build();
    }

    public static AnalysisResponse.AnalysisDto toAnalysisDto(Analysis analysis) {
        Record record = analysis.getRecord();

        // TODO: keyword 정렬 순서 고려 필요
        List<AbilityResponse.AbilityDto> abilityDtoList = analysis.getAbilityList().stream()
                .map(AbilityConverter::toAbilityDto)
                .toList();

        return AnalysisResponse.AnalysisDto.builder()
                .analysisId(analysis.getAnalysisId())
                .chatRoomId(record.getType() == RecordType.CHAT ? record.getChatRoom().getChatRoomId() : null)
                .recordId(record.getRecordId())
                .recordType(record.getType())
                .recordTitle(record.getTitle())
                .recordContent(analysis.getContent())
                .abilityDtoList(abilityDtoList)
                .comment(analysis.getComment())
                .createdAt(analysis.getCreatedAtFormatted())
                .build();
    }
}
