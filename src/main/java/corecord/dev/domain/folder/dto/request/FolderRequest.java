package corecord.dev.domain.folder.dto.request;

import lombok.Data;

public class FolderRequest {

    @Data
    public static class FolderDto {
        private String title;
    }

    @Data
    public static class FolderUpdateDto {
        private Long folderId;
        private String title;
    }
}
