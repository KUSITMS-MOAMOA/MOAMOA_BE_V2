package corecord.dev.domain.record.service;

import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.domain.analysis.constant.Keyword;
import corecord.dev.domain.analysis.exception.enums.AnalysisErrorStatus;
import corecord.dev.domain.analysis.exception.model.AnalysisException;
import corecord.dev.domain.analysis.service.AnalysisService;
import corecord.dev.domain.folder.entity.Folder;
import corecord.dev.domain.folder.exception.enums.FolderErrorStatus;
import corecord.dev.domain.folder.exception.model.FolderException;
import corecord.dev.domain.folder.repository.FolderRepository;
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

    /*
     * user의 MEMO ver. 경험을 기록하고 폴더를 지정한 후 생성된 경험 기록 정보를 반환
     * @param userId, recordDto
     * @return
     */
    @Transactional
    public RecordResponse.MemoRecordDto createMemoRecord(Long userId, RecordRequest.MemoRecordDto recordDto) {
        User user = findUserById(userId);
        String title = recordDto.getTitle();
        String content = recordDto.getContent();
        Folder folder = findFolderById(recordDto.getFolderId());

        // 제목, 본문 글자 수 검사
        validTextLength(title, content);

        // record(memo) 객체 생성 및 연관관계 설정
        Record record = RecordConverter.toMemoRecordEntity(title, content, user, folder);
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
        Record tmpMemoRecord = findRecordById(tmpMemoRecordId);

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
    public RecordResponse.RecordListDto getRecordList(Long userId, String folderName) {
        User user = findUserById(userId);
        List<Record> recordList;

        // 임시 저장 기록 제외 Record List 최신 생성 순 조회
        if (folderName.equals("all")) {
            recordList = getRecordList(user);
        } else {
            Folder folder = findFolderByTitle(user, folderName);
            recordList = getRecordListByFolder(user, folder);
        }

        return RecordConverter.toRecordListDto(folderName, recordList);
    }

    /*
     * keyword를 받아 해당 키워드를 가진 역량 분석 정보와 경험 기록 정보를 반환
     * @param userId, keywordValue
     * @return
     */
    @Transactional(readOnly = true)
    public RecordResponse.KeywordRecordListDto getKeywordRecordList(Long userId, String keywordValue) {
        User user = findUserById(userId);

        // 해당 keyword를 가진 ability 객체 조회 후 맵핑된 Record 객체 리스트 조회
        Keyword keyword = getKeyword(keywordValue);
        List<Record> recordList = getRecordListByKeyword(user, keyword);

        return RecordConverter.toKeywordRecordListDto(recordList);
    }

    private void validHasUserTmpMemo(User user) {
        if (user.getTmpMemo() != null)
            throw new RecordException(RecordErrorStatus.ALREADY_TMP_MEMO);
    }

    private void validTextLength(String title, String content) {
        if (title != null && title.length() > 15)
            throw new RecordException(RecordErrorStatus.OVERFLOW_MEMO_RECORD_TITLE);

        if (content != null && content.length() > 200) {
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

    private List<Record> getRecordListByFolder(User user, Folder folder) {
        return recordRepository.findRecordsByFolder(folder, user);
    }

    private List<Record> getRecordList(User user) {
        return recordRepository.findRecords(user);
    }

    private List<Record> getRecordListByKeyword(User user, Keyword keyword) {
        return recordRepository.findRecordsByKeyword(keyword, user);
    }

    private Keyword getKeyword(String keywordValue) {
        Keyword keyword = Keyword.getName(keywordValue);
        if (keyword == null)
            throw new AnalysisException(AnalysisErrorStatus.INVALID_KEYWORD);
        return keyword;
    }
}
