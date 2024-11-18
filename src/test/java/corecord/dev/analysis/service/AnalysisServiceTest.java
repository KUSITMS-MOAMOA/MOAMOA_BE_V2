package corecord.dev.analysis.service;

import corecord.dev.domain.ability.entity.Ability;
import corecord.dev.domain.ability.entity.Keyword;
import corecord.dev.domain.ability.service.AbilityService;
import corecord.dev.domain.analysis.dto.response.AnalysisAiResponse;
import corecord.dev.domain.analysis.entity.Analysis;
import corecord.dev.domain.analysis.exception.enums.AnalysisErrorStatus;
import corecord.dev.domain.analysis.exception.model.AnalysisException;
import corecord.dev.domain.analysis.repository.AnalysisRepository;
import corecord.dev.domain.analysis.service.AnalysisService;
import corecord.dev.domain.analysis.service.OpenAiService;
import corecord.dev.domain.folder.entity.Folder;
import corecord.dev.domain.record.constant.RecordType;
import corecord.dev.domain.record.entity.Record;
import corecord.dev.domain.user.entity.Status;
import corecord.dev.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class AnalysisServiceTest {

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private OpenAiService openAiService;

    @Mock
    private AbilityService abilityService;

    @InjectMocks
    private AnalysisService analysisService;

    private User user;
    private Folder folder;
    private Record record;

    private String testTitle = "Test Record";
    private String testContent = "Test".repeat(10);
    private String testComment = "Test Comment";

    @BeforeEach
    void setUp() {
        user = createMockUser();
        folder = createMockFolder(user);
        record = createMockRecord(user, folder);
    }

    @Test
    @DisplayName("메모 역량 분석 생성 테스트")
    void createMemoAnalysisTest() {
        // Given
        Analysis analysis = createMockAnalysis(record);

        when(openAiService.generateMemoSummary(any(String.class))).thenReturn(testContent);
        when(openAiService.generateAbilityAnalysis(any(String.class)))
                .thenReturn(new AnalysisAiResponse(Map.of("커뮤니케이션", "Test Keyword Content"), "Test Comment"));
        when(analysisRepository.save(any(Analysis.class))).thenReturn(analysis);
        doNothing().when(abilityService).parseAndSaveAbilities(any(Map.class), any(Analysis.class), any(User.class));

        // When
        Analysis response = analysisService.createAnalysis(record, user);

        // Then
        verify(openAiService).generateMemoSummary(testContent);
        verify(openAiService).generateAbilityAnalysis(testContent);
        verify(analysisRepository).save(any(Analysis.class));

        assertEquals(response.getContent(), testContent);
        assertEquals(response.getComment(), testComment);
    }

    @Test
    @DisplayName("메모 역량 분석 요약 글자수 예외 발생 테스트")
    void createMemoAnalysisWithNotEnoughContentTest() {
        // Given
        String overContent = "Test".repeat(500);
        when(openAiService.generateMemoSummary(any(String.class))).thenReturn(overContent);

        // When & Then
        AnalysisException exception = assertThrows(AnalysisException.class,
                () -> analysisService.createAnalysis(record, user));
        assertEquals(exception.getAnalysisErrorStatus(), AnalysisErrorStatus.OVERFLOW_ANALYSIS_CONTENT);
    }

    @Test
    @DisplayName("메모 역량 분석 코멘트 글자수 예외 발생 테스트")
    void createMemoAnalysisWithNotEnoughCommentTest() {
        // Given
        String overComment = "Test".repeat(200);
        when(openAiService.generateMemoSummary(any(String.class))).thenReturn(testContent);
        when(openAiService.generateAbilityAnalysis(any(String.class)))
                .thenReturn(new AnalysisAiResponse(Map.of("커뮤니케이션", "Test Keyword Content"), overComment));

        // When & Then
        AnalysisException exception = assertThrows(AnalysisException.class,
                () -> analysisService.createAnalysis(record, user));
        assertEquals(exception.getAnalysisErrorStatus(), AnalysisErrorStatus.OVERFLOW_ANALYSIS_COMMENT);
    }

    @Test
    @DisplayName("메모 역량 분석 키워드 글자수 예외 발생 테스트")
    void createMemoAnalysisWithLongKeywordCommentTest() {
        // Given
        String overKeywordComment = "Test".repeat(200);
        when(openAiService.generateMemoSummary(any(String.class))).thenReturn(testContent);
        when(openAiService.generateAbilityAnalysis(any(String.class)))
                .thenReturn(new AnalysisAiResponse(Map.of("커뮤니케이션", overKeywordComment), testComment));

        // When & Then
        AnalysisException exception = assertThrows(AnalysisException.class,
                () -> analysisService.createAnalysis(record, user));
        assertEquals(exception.getAnalysisErrorStatus(), AnalysisErrorStatus.OVERFLOW_ANALYSIS_KEYWORD_CONTENT);
    }

    @Test
    @DisplayName("존재하지 않는 키워드 제시 시 예외 발생 테스트")
    void findAbilityByNotExistingKeyword() {
        // Given

        // When & Then

    }


    @Test
    @DisplayName("메모 역량 분석 재생성 테스트")
    void recreateAnalysisTest() {
        // Given
        // When
        // Then
    }

    @Test
    @DisplayName("역량 분석 상세 정보 조회 테스트")
    void getAnalysisDetailTest() {
        // Given
        // When
        // Then

    }

    @Test
    @DisplayName("역량 분석 수정 테스트")
    void updateAnalysisTest() {
        // Given
        // When
        // Then

    }

    @Test
    @DisplayName("역량 분석 삭제 테스트")
    void deleteAnalysisTest() {
        // Given
        // When
        // Then

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
                .comment(testComment)
                .record(record)
                .build();
    }

    private Ability createMockAbility(Analysis analysis) {
        return Ability.builder()
                .keyword(Keyword.COMMUNICATION)
                .content("Test Keyword Content")
                .user(user)
                .analysis(analysis)
                .build();
    }

}
