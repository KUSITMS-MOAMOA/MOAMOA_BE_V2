package corecord.dev.domain.folder.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

public class FolderResponse {

    @Builder @Getter
    @AllArgsConstructor @Data
    public static class FolderDto {
        private Long folderId;
        private String title;
    }

    @Builder @Getter
    @AllArgsConstructor @Data
    public static class FolderDtoList {
        private List<FolderDto> folderDtoList;
    }
}
