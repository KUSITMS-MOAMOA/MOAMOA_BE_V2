package corecord.dev.domain.folder.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class FolderRequest {

    @Data
    public static class FolderDto {
        @NotBlank(message = "폴더 명을 입력해주세요.")
        private String title;
    }

    @Data
    public static class FolderUpdateDto {
        @NotBlank(message = "수정할 폴더 id를 입력해주세요.")
        private Long folderId;
        @NotBlank(message = "수정할 폴더 명을 입력해주세요.")
        private String title;
    }
}
