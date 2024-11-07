package corecord.dev.domain.record.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

public class RecordResponse {
    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class MemoRecordDto {
        private Long recordId;
        private String title;
        private String content;
        private String folder;
        private String createdAt;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class TmpMemoRecordDto {
        private Boolean isExist;
        private String title;
        private String content;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class RecordDto {
        private Long analysisId;
        private Long recordId;
        private String folder;
        private String title;
        private List<String> keywordList;
        private String createdAt;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class RecordListDto {
        private String folder;
        private List<RecordDto> recordDtoList;
        private boolean hasNext;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class KeywordRecordDto {
        private Long analysisId;
        private Long recordId;
        private String folder;
        private String title;
        private String content;
        private String createdAt;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @Data
    public static class KeywordRecordListDto {
        private List<KeywordRecordDto> recordDtoList;
        private boolean hasNext;
    }
}
