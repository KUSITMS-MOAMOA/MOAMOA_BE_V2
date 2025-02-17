package corecord.dev.domain.folder.application;

import corecord.dev.domain.folder.domain.dto.response.FolderResponse;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.folder.domain.repository.FolderRepository;
import corecord.dev.domain.folder.exception.FolderException;
import corecord.dev.domain.folder.status.FolderErrorStatus;
import corecord.dev.domain.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderDbService {
    private final FolderRepository folderRepository;

    @Transactional
    public void saveFolder(Folder folder) {
        folderRepository.save(folder);
    }

    @Transactional
    public void deleteFolder(Folder folder) {
        folderRepository.delete(folder);
    }

    @Transactional
    public void deleteFolderByUserId(Long userId) {
        folderRepository.deleteFolderByUserId(userId);
    }

    public Folder findFolderByTitle(Long userId, String title) {
        return folderRepository.findFolderByTitle(title, userId)
                .orElseThrow(() -> new FolderException(FolderErrorStatus.FOLDER_NOT_FOUND));
    }

    public Folder findFolderById(Long folderId) {
        return folderRepository.findById(folderId)
                .orElseThrow(() -> new FolderException(FolderErrorStatus.FOLDER_NOT_FOUND));
    }

    public List<FolderResponse.FolderDto> findFolderDtoList(Long userId) {
        return folderRepository.findFolderDtoList(userId);
    }

    public boolean isFolderExist(String title, User user) {
        return folderRepository.existsByTitleAndUser(title, user);
    }
}
