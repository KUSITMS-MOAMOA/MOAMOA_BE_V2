package corecord.dev.domain.record.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

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
}
