package corecord.dev.domain.record.service;

import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
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
        analysisService.getAnalysis(record);

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

    private void validTextLength(String title, String content) {
        if (title.length() > 15) {
            throw new RecordException(RecordErrorStatus.OVERFLOW_MEMO_RECORD_TITLE);
        }

        if (content.length() > 200) {
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

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.UNAUTHORIZED));
    }

    private Record findRecordById(Long recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new RecordException(RecordErrorStatus.RECORD_NOT_FOUND));
    }
}
