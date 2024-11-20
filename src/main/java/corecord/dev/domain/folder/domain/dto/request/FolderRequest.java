package corecord.dev.domain.folder.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class FolderRequest {

    @Data @Builder
    @AllArgsConstructor @NoArgsConstructor
    public static class FolderDto {
        @NotBlank(message = "폴더 명을 입력해주세요.")
        private String title;
    }

    @Data @Builder
    @AllArgsConstructor @NoArgsConstructor
    public static class FolderUpdateDto {
        @NotNull(message = "수정할 폴더 id를 입력해주세요.")
        private Long folderId;
        @NotBlank(message = "수정할 폴더 명을 입력해주세요.")
        private String title;
    }
}
