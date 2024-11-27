package corecord.dev.record.memo.service;

import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.analysis.application.AnalysisService;
import corecord.dev.domain.folder.application.FolderDbService;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.record.application.RecordDbService;
import corecord.dev.domain.record.domain.entity.RecordType;
import corecord.dev.domain.record.domain.dto.request.RecordRequest;
import corecord.dev.domain.record.domain.dto.response.RecordResponse;
import corecord.dev.domain.record.domain.entity.Record;
import corecord.dev.domain.record.domain.repository.RecordRepository;
import corecord.dev.domain.record.status.RecordErrorStatus;
import corecord.dev.domain.record.exception.RecordException;
import corecord.dev.domain.record.application.RecordService;
import corecord.dev.domain.user.application.UserDbService;
import corecord.dev.domain.user.domain.entity.Status;
import corecord.dev.domain.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class MemoRecordServiceTest {

    @Mock
    private UserDbService userDbService;

    @Mock
    private FolderDbService folderDbService;

    @Mock
    private RecordDbService recordDbService;

    @Mock
    private AnalysisService analysisService;

    @InjectMocks
    private RecordService recordService;

    private User user;
    private Folder folder;

    private String testTitle = "Test Record";
    private String testContent = "Test".repeat(10);

    @BeforeEach
    void setUp() {
        user = createMockUser();
        folder = createMockFolder(user);
    }
    @Test
    @DisplayName("메모 경험 기록 생성 테스트")
    void createMemoRecordTest() {
        // Given
        Record record = createMockRecord(user, folder);

        when(userDbService.findUserById(1L)).thenReturn(user);
        when(folderDbService.findFolderById(1L)).thenReturn(folder);
        when(analysisService.createAnalysis(any(Record.class), any(User.class)))
                .thenReturn(createMockAnalysis(record));
        when(recordDbService.saveRecord(any(Record.class))).thenAnswer(invocation -> {
            Record savedRecord = invocation.getArgument(0);
            savedRecord.setCreatedAt(LocalDateTime.now());
            return savedRecord;
        });

        // When
        RecordRequest.RecordDto request = RecordRequest.RecordDto.builder()
                    .title(testTitle)
                    .content(testContent)
                    .folderId(1L)
                    .recordType(RecordType.MEMO)
                    .build();

        RecordResponse.MemoRecordDto response = recordService.createMemoRecord(1L, request);

        // Then
        verify(userDbService).findUserById(1L);
        verify(folderDbService).findFolderById(1L);
        verify(recordDbService).saveRecord(any(Record.class));

        assertEquals(response.getFolder(), folder.getTitle());
        assertEquals(response.getTitle(), testTitle);
        assertEquals(response.getContent(), testContent);
    }

    @Test
    @DisplayName("경험 기록 제목이 긴 경우 예외 발생")
    void createMemoRecordWithLongContent() {
        // Given
        when(userDbService.findUserById(1L)).thenReturn(user);
        when(folderDbService.findFolderById(1L)).thenReturn(folder);

        // When & Then
        RecordRequest.RecordDto request = RecordRequest.RecordDto.builder()
                .title("a".repeat(51))
                .content(testContent)
                .folderId(1L)
                .recordType(RecordType.MEMO)
                .build();

        RecordException exception = assertThrows(RecordException.class,
                () -> recordService.createMemoRecord(1L, request));
        assertEquals(exception.getRecordErrorStatus(), RecordErrorStatus.OVERFLOW_MEMO_RECORD_TITLE);
    }

    @Test
    @DisplayName("경험 기록 내용 글자수가 충분하지 않은 경우 예외 발생")
    void createMemoRecordWithNotEnoughContent() {
        // Given
        when(userDbService.findUserById(1L)).thenReturn(user);
        when(folderDbService.findFolderById(1L)).thenReturn(folder);

        // When & Then
        RecordRequest.RecordDto request = RecordRequest.RecordDto.builder()
                .title(testTitle)
                .content("Test")
                .folderId(1L)
                .recordType(RecordType.MEMO)
                .build();

        RecordException exception = assertThrows(RecordException.class,
                () -> recordService.createMemoRecord(1L, request));
        assertEquals(exception.getRecordErrorStatus(), RecordErrorStatus.NOT_ENOUGH_MEMO_RECORD_CONTENT);
    }

    @Test
    @DisplayName("임시 메모 경험 기록 저장 테스트")
    void createTmpMemoRecordTest() {
        // Given
        Record tmpRecord = createMockRecord(user, null);
        when(userDbService.findUserById(1L)).thenReturn(user);
        when(recordDbService.saveRecord(any(Record.class))).thenReturn(tmpRecord);

        // When
        RecordRequest.TmpMemoRecordDto request = RecordRequest.TmpMemoRecordDto.builder()
                .title(testTitle)
                .content(testContent)
                .build();

        recordService.createTmpMemoRecord(1L, request);

        // Then
        verify(userDbService, times(1)).findUserById(1L);
        verify(recordDbService, times(1)).saveRecord(any(Record.class));
        assertEquals(user.getTmpMemo(), tmpRecord.getRecordId());
    }

    @Test
    @DisplayName("중복 임시 메모 경험 기록 저장 시 예외 발생 테스트")
    void createTmpMemoRecordDuplicateTest() {
        // Given
        user.updateTmpMemo(1L); // 이미 임시 메모 경험 기록을 저장
        when(userDbService.findUserById(1L)).thenReturn(user);

        // When & Then
        RecordRequest.TmpMemoRecordDto request = RecordRequest.TmpMemoRecordDto.builder()
                .title(testTitle)
                .content(testContent)
                .build();

        RecordException exception = assertThrows(RecordException.class,
                () -> recordService.createTmpMemoRecord(1L, request));
        assertEquals(exception.getRecordErrorStatus(), RecordErrorStatus.ALREADY_TMP_MEMO);

        verify(userDbService, times(1)).findUserById(1L);
        verify(recordDbService, times(0)).saveRecord(any(Record.class));
    }

    @Test
    @DisplayName("임시 메모 경험 기록이 있는 경우 조회 테스트")
    void getTmpMemoRecordTest() {
        // Given
        Record tmpRecord = createMockRecord(user, null);
        user.updateTmpMemo(1L);

        when(userDbService.findUserById(1L)).thenReturn(user);
        when(recordDbService.findTmpRecordById(1L)).thenReturn(tmpRecord);

        // When
        RecordResponse.TmpMemoRecordDto response = recordService.getTmpMemoRecord(1L);

        // Then
        verify(userDbService, times(1)).findUserById(1L);
        verify(recordDbService, times(1)).findTmpRecordById(1L);
        verify(recordDbService, times(1)).deleteRecord(tmpRecord);

        assertNull(user.getTmpMemo());
        assertTrue(response.getIsExist());
        assertEquals(response.getTitle(), testTitle);
        assertEquals(response.getContent(), testContent);
    }

    @Test
    @DisplayName("임시 메모 경험 기록이 없는 경우 조회 테스트")
    void getTmpMemoRecordWithoutRecordTest() {
        // Given
        user.updateTmpMemo(null);
        when(userDbService.findUserById(1L)).thenReturn(user);

        // When
        RecordResponse.TmpMemoRecordDto response = recordService.getTmpMemoRecord(1L);

        // Then
        verify(userDbService, times(1)).findUserById(1L);

        assertFalse(response.getIsExist());
        assertNull(response.getTitle());
        assertNull(response.getContent());
    }

    private User createMockUser() {
        return User.builder()
                .userId(1L)
                .providerId("Test Provider")
                .nickName("Test User")
                .status(Status.GRADUATE_STUDENT)
                .folders(new ArrayList<>())
                .build();
    }

    private Folder createMockFolder(User user) {
        return Folder.builder()
                .folderId(1L)
                .title("Test Folder")
                .user(user)
                .build();
    }

    private Record createMockRecord(User user, Folder folder) {
        return Record.builder()
                .recordId(1L)
                .title(testTitle)
                .content(testContent)
                .user(user)
                .type(RecordType.MEMO)
                .folder(folder)
                .build();
    }

    private Analysis createMockAnalysis(Record record) {
        return Analysis.builder()
                .analysisId(1L)
                .content(testContent)
                .comment("Test Comment")
                .record(record)
                .build();
    }

}
