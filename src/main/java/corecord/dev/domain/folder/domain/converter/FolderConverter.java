package corecord.dev.domain.folder.domain.converter;

import corecord.dev.domain.folder.domain.dto.response.FolderResponse;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.user.domain.entity.User;

import java.util.List;

public class FolderConverter {

    public static Folder toFolderEntity(String title, User user) {
        return Folder.builder()
                .title(title)
                .user(user)
                .build();
    }

    public static FolderResponse.FolderDto toFolderDto(Folder folder) {
        return FolderResponse.FolderDto.builder()
                .folderId(folder.getFolderId())
                .title(folder.getTitle())
                .build();
    }

    public static FolderResponse.FolderDtoList toFolderDtoList(List<FolderResponse.FolderDto> folderDtoList) {
        return FolderResponse.FolderDtoList.builder()
                .folderDtoList(folderDtoList)
                .build();
    }
}
