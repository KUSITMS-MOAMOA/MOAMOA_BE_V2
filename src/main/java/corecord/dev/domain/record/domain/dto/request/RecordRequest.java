package corecord.dev.domain.record.domain.dto.request;

import corecord.dev.domain.record.domain.enums.RecordType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class RecordRequest {
    @Data @Builder
    public static class RecordDto {
        @NotBlank(message = "제목을 입력해주세요.")
        private String title;
        @NotBlank(message = "내용을 입력해주세요.")
        private String content;
        @NotNull(message = "저장할 폴더의 id를 입력해주세요.")
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
        @NotNull(message = "변경할 경험 기록의 id를 입력해주세요.")
        private Long recordId;
        @NotBlank(message = "변경할 폴더를 입력해주세요.")
        private String folder;
    }
}
