package corecord.dev.domain.folder.service;

import corecord.dev.domain.folder.converter.FolderConverter;
import corecord.dev.domain.folder.dto.request.FolderRequest;
import corecord.dev.domain.folder.dto.response.FolderResponse;
import corecord.dev.domain.folder.entity.Folder;
import corecord.dev.domain.folder.exception.enums.FolderErrorStatus;
import corecord.dev.domain.folder.exception.model.FolderException;
import corecord.dev.domain.folder.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;

    /*
     * 폴더명(title)을 request로 받아, 새로운 폴더를 생성
     * @param folderDto
     * @return
     */
    @Transactional
    public FolderResponse.FolderDtoList createFolder(FolderRequest.FolderDto folderDto) {
        validateDuplicatedFolderTitle(folderDto.getTitle());

        Folder folder = FolderConverter.toFolderEntity(folderDto.getTitle());
        folderRepository.save(folder);

        return getFolderList();
    }

    /*
     * folderId를 통해 folder을 삭제
     * @param folderId
     * @return
     */
    @Transactional
    public FolderResponse.FolderDtoList deleteFolder(Long folderId) {
        Folder folder = findFolderById(folderId);
        folderRepository.delete(folder);

        return getFolderList();
    }

    /*
     * folderId를 받아, 해당 folder의 title을 수정
     * @param folderDto
     * @return
     */
    @Transactional
    public FolderResponse.FolderDtoList updateFolder(FolderRequest.FolderUpdateDto folderDto) {
        Folder folder = findFolderById(folderDto.getFolderId());

        validateDuplicatedFolderTitle(folderDto.getTitle());
        validateTitleLength(folderDto.getTitle());

        folder.updateTitle(folderDto.getTitle());

        return getFolderList();
    }

    /*
     * 생성일 오름차순으로 폴더 리스트를 조회
     * @return
     */
    @Transactional(readOnly = true)
    public FolderResponse.FolderDtoList getFolderList() {
        List<FolderResponse.FolderDto> folderList = folderRepository.findFolderDtoList();
        return FolderConverter.toFolderDtoList(folderList);
    }

    // 폴더명 중복 검사
    private void validateDuplicatedFolderTitle(String title) {
        if (folderRepository.existsByTitle(title)) {
            throw new FolderException(FolderErrorStatus.DUPLICATED_FOLDER_TITLE);
        }
    }

    // title 글자 수 검사
    private void validateTitleLength(String title) {
        if (title.length() > 15) {
            throw new FolderException(FolderErrorStatus.OVERFLOW_FOLDER_TITLE);
        }
    }

    private Folder findFolderById(Long folderId) {
        return folderRepository.findById(folderId)
                .orElseThrow(() -> new FolderException(FolderErrorStatus.FOLDER_NOT_FOUND));
    }
}
