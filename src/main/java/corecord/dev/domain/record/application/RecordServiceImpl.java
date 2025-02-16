package corecord.dev.domain.record.application;

import corecord.dev.domain.ability.domain.entity.Keyword;
import corecord.dev.domain.ability.exception.AbilityException;
import corecord.dev.domain.ability.status.AbilityErrorStatus;
import corecord.dev.domain.analysis.application.AnalysisService;
import corecord.dev.domain.chat.application.ChatDbService;
import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.folder.application.FolderDbService;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.record.domain.converter.RecordConverter;
import corecord.dev.domain.record.domain.dto.request.RecordRequest;
import corecord.dev.domain.record.domain.dto.response.RecordResponse;
import corecord.dev.domain.record.domain.entity.Record;
import corecord.dev.domain.record.domain.entity.RecordType;
import corecord.dev.domain.record.exception.RecordException;
import corecord.dev.domain.record.status.RecordErrorStatus;
import corecord.dev.domain.user.application.UserDbService;
import corecord.dev.domain.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final AnalysisService analysisService;
    private final RecordDbService recordDbService;
    private final UserDbService userDbService;
    private final FolderDbService folderDbService;
    private final ChatDbService chatDbService;

    private final int listSize = 30;

    /**
     * user의 MEMO ver. 경험을 기록하고 폴더를 지정한 후 생성된 경험 기록 정보를 반환합니다.
     *
     * @param userId, recordDto
     * @return recordId, title, content, folderName, createdAt
     */
    @Override
    public RecordResponse.MemoRecordDto createMemoRecord(Long userId, RecordRequest.RecordDto recordDto) {
        User user = userDbService.findUserById(userId);
        String title = recordDto.getTitle();
        String content = recordDto.getContent();
        Folder folder = folderDbService.findFolderById(recordDto.getFolderId());

        // 제목, 본문 글자 수 검사
        validTextLength(title, content);

        // 경험 기록 종류에 따른 Record 생성
        Record record = createRecordBasedOnType(recordDto, user, folder);

        // 역량 분석 레포트 생성
        analysisService.createAnalysis(record, user);
        recordDbService.saveRecord(record);

        return RecordConverter.toMemoRecordDto(record);
    }

    private Record createRecordBasedOnType(RecordRequest.RecordDto recordDto, User user, Folder folder) {
        if (recordDto.getRecordType() == RecordType.MEMO)
            return RecordConverter.toMemoRecordEntity(recordDto.getTitle(), recordDto.getContent(), user, folder);

        ChatRoom chatRoom = chatDbService.findChatRoomById(recordDto.getChatRoomId(), user.getUserId());
        return RecordConverter.toChatRecordEntity(recordDto.getTitle(), recordDto.getContent(), user, folder, chatRoom);
    }

    /**
     * recordId를 받아 MEMO ver. 경험 기록의 상세 정보를 반환합니다.
     *
     * @param userId, recordId
     * @return recordId, title, content, folderName, createdAt
     */
    @Override
    @Transactional(readOnly = true)
    public RecordResponse.MemoRecordDto getMemoRecordDetail(Long userId, Long recordId) {
        Record record = recordDbService.findRecordById(recordId);
        validIsUserAuthorizedForRecord(userId, record);
        return RecordConverter.toMemoRecordDto(record);
    }

    private void validIsUserAuthorizedForRecord(Long userId, Record record) {
        if (!record.getUser().getUserId().equals(userId))
            throw new RecordException(RecordErrorStatus.USER_RECORD_UNAUTHORIZED);
    }

    /**
     * title, content를 받아 Record 객체를 생성한 후, recordId를 User.tmpMemo에 저장합니다.
     *
     * @param userId, tmpMemoRecordDto
     */
    @Transactional
    public void createTmpMemoRecord(Long userId, RecordRequest.TmpMemoRecordDto tmpMemoRecordDto) {
        User user = userDbService.findUserById(userId);
        String title = tmpMemoRecordDto.getTitle();
        String content = tmpMemoRecordDto.getContent();

        // User의 임시 메모 저장 유무 확인
        validHasUserTmpMemo(user);

        validTextLength(title, content);

        // Record entity 생성 후 user.tmpMemo 필드에 recordId 저장
        Record record = RecordConverter.toMemoRecordEntity(title, content, user, null);
        Record tmpRecord = recordDbService.saveRecord(record);
        user.updateTmpMemo(tmpRecord.getRecordId());
    }

    private void validHasUserTmpMemo(User user) {
        if (user.getTmpMemo() != null)
            throw new RecordException(RecordErrorStatus.ALREADY_TMP_MEMO);
    }

    /**
     * user의 임시 저장된 메모 기록 정보를 반환합니다.
     * 해당 Record data와 tmpMemo 필드 정보를 제거합니다.
     *
     * @param userId
     * @return 임시 메모 존재 여부, 메모 title, content
     */
    @Override
    @Transactional
    public RecordResponse.TmpMemoRecordDto getTmpMemoRecord(Long userId) {
        User user = userDbService.findUserById(userId);
        Long tmpMemoRecordId = user.getTmpMemo();

        // 임시 저장 내역이 없는 경우 isExist=false 반환
        if (tmpMemoRecordId == null) {
            return RecordConverter.toNotExistingTmpMemoRecordDto();
        }

        // 임시 저장 내역이 있는 경우 결과 조회
        Record tmpMemoRecord = recordDbService.findTmpRecordById(tmpMemoRecordId);

        // 기존 데이터 제거 후 결과 반환
        user.deleteTmpMemo();
        recordDbService.deleteRecord(tmpMemoRecord);
        return RecordConverter.toExistingTmpMemoRecordDto(tmpMemoRecord);
    }

    /**
     * 폴더별 경험 기록 리스트를 반환합니다.
     * folder의 default value: 'all'
     *
     * @param userId, folderName
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public RecordResponse.RecordListDto getRecordListByFolder(Long userId, String folderName, Long lastRecordId) {
        List<Record> recordList = fetchRecords(userId, folderName, lastRecordId);

        // 다음 조회할 데이터가 남아있는지 확인
        boolean hasNext = recordList.size() == listSize + 1;
        if (hasNext)
            recordList = recordList.subList(0, listSize);

        return RecordConverter.toRecordListDto(folderName, recordList, hasNext);
    }

    private List<Record> fetchRecords(Long userId, String folderName, Long lastRecordId) {
        if (folderName.equals("all")) {
            return recordDbService.findRecordList(userId, lastRecordId);
        }
        Folder folder = folderDbService.findFolderByTitle(userId, folderName);
        return recordDbService.findRecordListByFolder(userId, folder, lastRecordId);
    }

    /**
     * keyword를 받아 해당 키워드를 가진 역량 분석 정보와 경험 기록 정보를 반환합니다.
     *
     * @param userId
     * @param keywordValue
     * @param lastRecordId
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public RecordResponse.KeywordRecordListDto getRecordListByKeyword(Long userId, String keywordValue, Long lastRecordId) {
        // 해당 keyword를 가진 ability 객체 조회 후 맵핑된 Record 객체 리스트 조회
        Keyword keyword = getKeyword(keywordValue);
        List<Record> recordList = recordDbService.findRecordListByKeyword(userId, keyword, lastRecordId);

        // 다음 조회할 데이터가 남아있는지 확인
        boolean hasNext = recordList.size() == listSize + 1;
        if (hasNext)
            recordList = recordList.subList(0, listSize);

        return RecordConverter.toKeywordRecordListDto(recordList, hasNext);
    }

    private Keyword getKeyword(String keywordValue) {
        return Optional.ofNullable(Keyword.getName(keywordValue))
                .orElseThrow(() -> new AbilityException(AbilityErrorStatus.INVALID_KEYWORD));
    }

    /**
     * 최근 생성된 경험 기록 리스트 6개를 반환합니다.
     *
     * @param userId
     * @return RecordListDto
     */
    public RecordResponse.RecordListDto getRecentRecordList(Long userId) {
        List<Record> recordList = recordDbService.findRecordListOrderByCreatedAt(userId);
        return RecordConverter.toRecordListDto("all", recordList, false);
    }

    private void validTextLength(String title, String content) {
        if (title != null && title.length() > 50)
            throw new RecordException(RecordErrorStatus.OVERFLOW_MEMO_RECORD_TITLE);

        if (content != null && content.length() < 50)
            throw new RecordException(RecordErrorStatus.NOT_ENOUGH_MEMO_RECORD_CONTENT);

        if (content != null && content.length() > 500) {
            throw new RecordException(RecordErrorStatus.OVERFLOW_MEMO_RECORD_CONTENT);
        }
    }

    /**
     * record가 속한 폴더를 변경합니다.
     *
     * @param userId
     * @param updateFolderDto
     */
    @Transactional
    public void updateFolderOfRecord(Long userId, RecordRequest.UpdateFolderDto updateFolderDto) {
        Record record = recordDbService.findRecordById(updateFolderDto.getRecordId());
        Folder folder = folderDbService.findFolderByTitle(userId, updateFolderDto.getFolder());

        record.updateFolder(folder);
    }
}
