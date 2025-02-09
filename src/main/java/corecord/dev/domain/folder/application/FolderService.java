package corecord.dev.domain.folder.application;

import corecord.dev.domain.ability.application.AbilityDbService;
import corecord.dev.domain.analysis.application.AnalysisDbService;
import corecord.dev.domain.chat.application.ChatDbService;
import corecord.dev.domain.folder.domain.converter.FolderConverter;
import corecord.dev.domain.folder.domain.dto.request.FolderRequest;
import corecord.dev.domain.folder.domain.dto.response.FolderResponse;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.folder.status.FolderErrorStatus;
import corecord.dev.domain.folder.exception.FolderException;
import corecord.dev.domain.record.application.RecordDbService;
import corecord.dev.domain.user.application.UserDbService;
import corecord.dev.domain.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FolderService {
    private final FolderDbService folderDbService;
    private final UserDbService userDbService;
    private final AnalysisDbService analysisDbService;
    private final AbilityDbService abilityDbService;
    private final ChatDbService chatDbService;
    private final RecordDbService recordDbService;


    /*
     * 폴더명(title)을 request로 받아, 새로운 폴더를 생성 후 생성 순 풀더 리스트 반환
     * @param userId, folderDto
     * @return
     */
    @Transactional
    public FolderResponse.FolderDtoList createFolder(Long userId, FolderRequest.FolderDto folderDto) {
        User user = userDbService.findUserById(userId);
        String title = folderDto.getTitle();

        // 폴더명 유효성 검증
        validDuplicatedFolderTitleAndLength(title, user);

        // folder 객체 생성 및 User 연관관계 설정
        Folder folder = FolderConverter.toFolderEntity(title, user);
        folderDbService.saveFolder(folder);

        List<FolderResponse.FolderDto> folderList = folderDbService.findFolderDtoList(userId);
        return FolderConverter.toFolderDtoList(folderList);
    }

    /*
     * folderId를 받아 folder를 삭제한 후 생성 순 폴더 리스트 반환
     * @param userId, folderId
     * @return
     */
    @Transactional
    public FolderResponse.FolderDtoList deleteFolder(Long userId, Long folderId) {
        Folder folder = folderDbService.findFolderById(folderId);

        // User-Folder 권한 유효성 검증
        validIsUserAuthorizedForFolder(userId, folder);

        abilityDbService.deleteAbilityByFolder(folder);
        analysisDbService.deleteAnalysisByFolder(folder);
        chatDbService.deleteChatRoomByFolder(folder);
        recordDbService.deleteRecordByFolder(folder);
        folderDbService.deleteFolder(folder);

        List<FolderResponse.FolderDto> folderList = folderDbService.findFolderDtoList(userId);
        return FolderConverter.toFolderDtoList(folderList);
    }

    /*
     * folderId를 받아, 해당 folder의 title을 수정
     * @param userId, folderDto
     * @return
     */
    @Transactional
    public FolderResponse.FolderDtoList updateFolder(Long userId, FolderRequest.FolderUpdateDto folderDto) {
        User user = userDbService.findUserById(userId);
        Folder folder = folderDbService.findFolderById(folderDto.getFolderId());
        String title = folderDto.getTitle();

        // 폴더명 유효성 검증
        validDuplicatedFolderTitleAndLength(title, user);

        // User-Folder 권한 유효성 검증
        validIsUserAuthorizedForFolder(userId, folder);

        folder.updateTitle(title);

        List<FolderResponse.FolderDto> folderList = folderDbService.findFolderDtoList(userId);
        return FolderConverter.toFolderDtoList(folderList);
    }

    private void validIsUserAuthorizedForFolder(Long userId, Folder folder) {
        if (!folder.getUser().getUserId().equals(userId))
            throw new FolderException(FolderErrorStatus.USER_FOLDER_UNAUTHORIZED);
    }

    /*
     * 생성일 오름차순으로 폴더 리스트를 조회
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public FolderResponse.FolderDtoList getFolderList(Long userId) {
        List<FolderResponse.FolderDto> folderList = folderDbService.findFolderDtoList(userId);
        return FolderConverter.toFolderDtoList(folderList);
    }

    private void validDuplicatedFolderTitleAndLength(String title, User user) {
        // 폴더명 글자 수 검사
        if (title.length() > 15) {
            throw new FolderException(FolderErrorStatus.OVERFLOW_FOLDER_TITLE);
        }

        // 폴더명 중복 검사
        if (folderDbService.isFolderExist(title, user)) {
            throw new FolderException(FolderErrorStatus.DUPLICATED_FOLDER_TITLE);
        }
    }
}
