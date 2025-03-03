package corecord.dev.domain.folder.application;

import corecord.dev.domain.folder.domain.dto.request.FolderRequest;
import corecord.dev.domain.folder.domain.dto.response.FolderResponse;


public interface FolderService {

    FolderResponse.FolderDtoList createFolder(Long userId, FolderRequest.FolderDto folderDto);
    FolderResponse.FolderDtoList deleteFolder(Long userId, Long folderId);
    FolderResponse.FolderDtoList updateFolder(Long userId, FolderRequest.FolderUpdateDto folderDto);
    FolderResponse.FolderDtoList getFolderList(Long userId);

}
