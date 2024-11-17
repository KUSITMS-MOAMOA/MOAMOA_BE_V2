package corecord.dev.domain.record.service;

import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.domain.ability.entity.Keyword;
import corecord.dev.domain.ability.exception.enums.AbilityErrorStatus;
import corecord.dev.domain.ability.exception.model.AbilityException;
import corecord.dev.domain.analysis.service.AnalysisService;
import corecord.dev.domain.chat.entity.ChatRoom;
import corecord.dev.domain.chat.exception.enums.ChatErrorStatus;
import corecord.dev.domain.chat.exception.model.ChatException;
import corecord.dev.domain.chat.repository.ChatRoomRepository;
import corecord.dev.domain.folder.entity.Folder;
import corecord.dev.domain.folder.exception.enums.FolderErrorStatus;
import corecord.dev.domain.folder.exception.model.FolderException;
import corecord.dev.domain.folder.repository.FolderRepository;
import corecord.dev.domain.record.constant.RecordType;
import corecord.dev.domain.record.converter.RecordConverter;
import corecord.dev.domain.record.dto.request.RecordRequest;
import corecord.dev.domain.record.dto.response.RecordResponse;
import corecord.dev.domain.record.entity.Record;
import corecord.dev.domain.record.exception.enums.RecordErrorStatus;
import corecord.dev.domain.record.exception.model.RecordException;
import corecord.dev.domain.record.repository.RecordRepository;
import corecord.dev.domain.user.entity.User;
import corecord.dev.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecordService {
    private final RecordRepository recordRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final AnalysisService analysisService;
    private final ChatRoomRepository chatRoomRepository;
    private final int listSize = 30;

    /*
     * user의 MEMO ver. 경험을 기록하고 폴더를 지정한 후 생성된 경험 기록 정보를 반환
     * @param userId, recordDto
     * @return
     */
    @Transactional
    public RecordResponse.MemoRecordDto createMemoRecord(Long userId, RecordRequest.RecordDto recordDto) {
        User user = findUserById(userId);
        String title = recordDto.getTitle();
        String content = recordDto.getContent();
        Folder folder = findFolderById(recordDto.getFolderId());

        // 제목, 본문 글자 수 검사
        validTextLength(title, content);

        // 경험 기록 종류에 따른 Record 생성
        Record record = createRecordBasedOnType(recordDto, user, folder);

        // Record 저장
        recordRepository.save(record);

        // 역량 분석 레포트 생성
        analysisService.createAnalysis(record, user);

        return RecordConverter.toMemoRecordDto(record);
    }

    /*
     * recordId를 받아 MEMO ver. 경험 기록의 상세 정보를 반환
     * @param userId, recordId
     * @return
     */
    @Transactional(readOnly = true)
    public RecordResponse.MemoRecordDto getMemoRecordDetail(Long userId, Long recordId) {
        User user = findUserById(userId);
        Record record = findRecordById(recordId);

        // User-Record 권한 유효성 검증
        validIsUserAuthorizedForRecord(user, record);

        return RecordConverter.toMemoRecordDto(record);
    }

    /*
     * title, content를 받아 Record 객체를 생성한 후, recordId를 User.tmpMemo에 저장
     * @param userId, tmpMemoRecordDto
     */
    @Transactional
    public void createTmpMemoRecord(Long userId, RecordRequest.TmpMemoRecordDto tmpMemoRecordDto) {
        User user = findUserById(userId);
        String title = tmpMemoRecordDto.getTitle();
        String content = tmpMemoRecordDto.getContent();

        // User의 임시 메모 저장 유무 확인
        validHasUserTmpMemo(user);

        // 제목, 본문 글자 수 검사
        validTextLength(title, content);

        // Record entity 생성 후 user.tmpMemo 필드에 recordId 저장
        Record record = RecordConverter.toMemoRecordEntity(title, content, user, null);
        Record tmpRecord = recordRepository.save(record);
        user.updateTmpMemo(tmpRecord.getRecordId());
    }

    /*
     * user의 임시 저장된 메모 기록이 있다면 해당 Record row와 tmpMemo 필드 정보를 제거한 후 저장된 데이터를 반환
     * @param userId
     * @return
     */
    @Transactional
    public RecordResponse.TmpMemoRecordDto getTmpMemoRecord(Long userId) {
        User user = findUserById(userId);
        Long tmpMemoRecordId = user.getTmpMemo();

        // 임시 저장 내역이 없는 경우 isExist=false 반환
        if (tmpMemoRecordId == null) {
            return RecordConverter.toNotExistingTmpMemoRecordDto();
        }

        // 임시 저장 내역이 있는 경우 결과 조회
        Record tmpMemoRecord = findTmpRecordById(tmpMemoRecordId);

        // 기존 데이터 제거 후 결과 반환
        user.deleteTmpMemo();
        recordRepository.delete(tmpMemoRecord);
        return RecordConverter.toExistingTmpMemoRecordDto(tmpMemoRecord);
    }

    /*
     * 폴더별 경험 기록 리스트를 반환합니다. folder의 default value는 'all'입니다.
     * @param userId, folderName
     * @return
     */
    @Transactional(readOnly = true)
    public RecordResponse.RecordListDto getRecordList(Long userId, String folderName, Long lastRecordId) {
        User user = findUserById(userId);
        List<Record> recordList;

        // 임시 저장 기록 제외 Record List 최신 생성 순 조회
        if (folderName.equals("all")) {
            recordList = findRecordList(user, lastRecordId);
        } else {
            Folder folder = findFolderByTitle(user, folderName);
            recordList = findRecordListByFolder(user, folder, lastRecordId);
        }

        // 다음 조회할 데이터가 남아있는지 확인
        boolean hasNext = recordList.size() == listSize + 1;
        if (hasNext)
            recordList = recordList.subList(0, listSize);

        return RecordConverter.toRecordListDto(folderName, recordList, hasNext);
    }

    /*
     * keyword를 받아 해당 키워드를 가진 역량 분석 정보와 경험 기록 정보를 반환
     * @param userId, keywordValue
     * @return
     */
    @Transactional(readOnly = true)
    public RecordResponse.KeywordRecordListDto getKeywordRecordList(Long userId, String keywordValue, Long lastRecordId) {
        User user = findUserById(userId);

        // 해당 keyword를 가진 ability 객체 조회 후 맵핑된 Record 객체 리스트 조회
        Keyword keyword = getKeyword(keywordValue);
        List<Record> recordList = findRecordListByKeyword(user, keyword, lastRecordId);

        // 다음 조회할 데이터가 남아있는지 확인
        boolean hasNext = recordList.size() == listSize + 1;
        if (hasNext)
            recordList = recordList.subList(0, listSize);

        return RecordConverter.toKeywordRecordListDto(recordList, hasNext);
    }

    /*
     * record가 속한 폴더를 변경
     * @param userId, updateFolderDto
     */
    @Transactional
    public void updateFolder(Long userId, RecordRequest.UpdateFolderDto updateFolderDto) {
        User user = findUserById(userId);
        Record record = findRecordById(updateFolderDto.getRecordId());
        Folder folder = findFolderByTitle(user, updateFolderDto.getFolder());

        record.updateFolder(folder);
    }

    /*
     * 최근 생성된 경험 기록 리스트 3개를 반환
     * @param userId
     * @return
     */
    public RecordResponse.RecordListDto getRecentRecordList(Long userId) {
        User user = findUserById(userId);

        // 최근 생성된 3개의 데이터만 조회
        List<Record> recordList = findRecordListOrderByCreatedAt(user);

        return RecordConverter.toRecordListDto("all", recordList, false);
    }

    private void validHasUserTmpMemo(User user) {
        if (user.getTmpMemo() != null)
            throw new RecordException(RecordErrorStatus.ALREADY_TMP_MEMO);
    }

    private void validTextLength(String title, String content) {
        if (title != null && title.length() > 15)
            throw new RecordException(RecordErrorStatus.OVERFLOW_MEMO_RECORD_TITLE);

        if (content != null && content.length() > 500) {
            throw new RecordException(RecordErrorStatus.OVERFLOW_MEMO_RECORD_CONTENT);
        }
    }

    // user-record 권한 검사
    private void validIsUserAuthorizedForRecord(User user, Record record) {
        if (!record.getUser().equals(user))
            throw new RecordException(RecordErrorStatus.USER_RECORD_UNAUTHORIZED);
    }

    private Folder findFolderById(Long folderId) {
        return folderRepository.findById(folderId)
                .orElseThrow(() -> new FolderException(FolderErrorStatus.FOLDER_NOT_FOUND));
    }

    private Folder findFolderByTitle(User user, String title) {
        return folderRepository.findFolderByTitle(title, user)
                .orElseThrow(() -> new FolderException(FolderErrorStatus.FOLDER_NOT_FOUND));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.UNAUTHORIZED));
    }

    private Record findRecordById(Long recordId) {
        return recordRepository.findRecordById(recordId)
                .orElseThrow(() -> new RecordException(RecordErrorStatus.RECORD_NOT_FOUND));
    }

    private Record findTmpRecordById(Long recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new RecordException(RecordErrorStatus.RECORD_NOT_FOUND));
    }

    private List<Record> findRecordListByFolder(User user, Folder folder, Long lastRecordId) {
        Pageable pageable = PageRequest.of(0, listSize + 1, Sort.by("createdAt").descending());
        return recordRepository.findRecordsByFolder(folder, user, lastRecordId, pageable);
    }

    private List<Record> findRecordList(User user, Long lastRecordId) {
        Pageable pageable = PageRequest.of(0, listSize + 1, Sort.by("createdAt").descending());
        return recordRepository.findRecords(user, lastRecordId, pageable);
    }

    private List<Record> findRecordListOrderByCreatedAt(User user) {
        Pageable pageable = PageRequest.of(0, 9, Sort.by("createdAt").descending());
        return recordRepository.findRecordsOrderByCreatedAt(user, pageable);
    }

    private List<Record> findRecordListByKeyword(User user, Keyword keyword, Long lastRecordId) {
        Pageable pageable = PageRequest.of(0, listSize + 1, Sort.by("createdAt").descending());
        return recordRepository.findRecordsByKeyword(keyword, user, lastRecordId, pageable);
    }

    private Keyword getKeyword(String keywordValue) {
        Keyword keyword = Keyword.getName(keywordValue);
        if (keyword == null)
            throw new AbilityException(AbilityErrorStatus.INVALID_KEYWORD);
        return keyword;
    }

    private Record createRecordBasedOnType(RecordRequest.RecordDto recordDto, User user, Folder folder) {
        if (recordDto.getRecordType() == RecordType.MEMO) {
            return RecordConverter.toMemoRecordEntity(recordDto.getTitle(), recordDto.getContent(), user, folder);
        } else {
            ChatRoom chatRoom = findChatRoomById(recordDto.getChatRoomId(), user);
            return RecordConverter.toChatRecordEntity(recordDto.getTitle(), recordDto.getContent(), user, folder, chatRoom);
        }
    }

    private ChatRoom findChatRoomById(Long chatRoomId, User user) {
        return chatRoomRepository.findByChatRoomIdAndUser(chatRoomId, user)
                .orElseThrow(() -> new ChatException(ChatErrorStatus.CHAT_ROOM_NOT_FOUND));
    }
}
