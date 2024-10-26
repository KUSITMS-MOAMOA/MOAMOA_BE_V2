package corecord.dev.domain.record.dto.request;

import lombok.Data;

public class RecordRequest {
    @Data
    public static class MemoRecordDto {
        private String title;
        private String content;
        private Long folderId;
    }
}
