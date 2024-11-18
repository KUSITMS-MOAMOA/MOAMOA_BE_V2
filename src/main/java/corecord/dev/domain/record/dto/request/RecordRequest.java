package corecord.dev.domain.record.dto.request;

import corecord.dev.domain.record.constant.RecordType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

public class RecordRequest {
    @Data @Builder
    public static class RecordDto {
        @NotBlank(message = "제목을 입력해주세요.")
        private String title;
        @NotBlank(message = "내용을 입력해주세요.")
        private String content;
        @NotBlank(message = "저장할 폴더의 id를 입력해주세요.")
        private Long folderId;
        @NotBlank(message = "저장할 기록의 타입을 입력해주세요.")
        private RecordType recordType;
        private Long chatRoomId;
    }

    @Data @Builder
    public static class TmpMemoRecordDto {
        @NotBlank(message = "임시 저장할 기록의 제목을 입력해주세요.")
        private String title;
        @NotBlank(message = "임시 저장할 내용을 입력해주세요.")
        private String content;
    }

    @Data
    public static class UpdateFolderDto {
        @NotBlank(message = "변경할 경험 기록의 id를 입력해주세요.")
        private Long recordId;
        @NotBlank(message = "변경할 폴더를 입력해주세요.")
        private String folder;
    }
}
