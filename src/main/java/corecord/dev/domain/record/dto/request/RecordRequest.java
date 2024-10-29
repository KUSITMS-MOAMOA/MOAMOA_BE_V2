package corecord.dev.domain.record.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class RecordRequest {
    @Data
    public static class MemoRecordDto {
        @NotBlank(message = "제목을 입력해주세요.")
        private String title;
        @NotBlank(message = "내용을 입력해주세요.")
        private String content;
        @NotBlank(message = "저장할 폴더의 id를 입력해주세요.")
        private Long folderId;
    }

    @Data
    public static class TmpMemoRecordDto {
        private String title;
        @NotBlank(message = "임시 저장할 내용을 입력해주세요.")
        private String content;
    }
}
