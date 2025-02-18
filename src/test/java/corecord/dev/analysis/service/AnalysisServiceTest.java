package corecord.dev.analysis.service;

import corecord.dev.domain.ability.domain.entity.Ability;
import corecord.dev.domain.ability.domain.enums.Keyword;
import corecord.dev.domain.ability.status.AbilityErrorStatus;
import corecord.dev.domain.ability.exception.AbilityException;
import corecord.dev.domain.ability.application.AbilityService;
import corecord.dev.domain.analysis.application.AnalysisAIService;
import corecord.dev.domain.analysis.application.AnalysisDbService;
import corecord.dev.domain.analysis.domain.dto.request.AnalysisRequest;
import corecord.dev.domain.analysis.infra.openai.dto.response.AnalysisAiResponse;
import corecord.dev.domain.analysis.domain.dto.response.AnalysisResponse;
import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.analysis.status.AnalysisErrorStatus;
import corecord.dev.domain.analysis.exception.AnalysisException;
import corecord.dev.domain.analysis.application.AnalysisService;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.record.application.RecordDbService;
import corecord.dev.domain.record.domain.enums.RecordType;
import corecord.dev.domain.record.domain.entity.Record;
import corecord.dev.domain.user.application.UserDbService;
import corecord.dev.domain.user.domain.enums.Status;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalysisServiceTest {

    @Mock
    private RecordDbService recordDbService;

    @Mock
    private AnalysisDbService analysisDbService;

    @Mock
    private UserDbService userDbService;

    @Mock
    private AnalysisAIService analysisAIService;

    @Mock
    private AbilityService abilityService;

    @InjectMocks
    private AnalysisService analysisService;

    private User user;
    private Folder folder;
    private Record record;
    private Analysis analysis;

    private String testTitle = "Test Record";
    private String testContent = "Test".repeat(10);
    private String testComment = "Test Comment";

    @BeforeEach
    void setUp() {
        user = createMockUser();
        folder = createMockFolder(user);
        record = createMockRecord(user, folder);
        analysis = createMockAnalysis(record);
        analysis.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("메모 역량 분석 생성 테스트")
    void createMemoAnalysisTest() {
        // Given
        when(analysisAIService.generateMemoSummary(any(String.class))).thenReturn(testContent);
        when(analysisAIService.generateAbilityAnalysis(any(String.class)))
                .thenReturn(new AnalysisAiResponse(Map.of("커뮤니케이션", "Test Keyword Content"), "Test Comment"));
        doNothing().when(analysisDbService).saveAnalysis(any(Analysis.class));
        doNothing().when(abilityService).parseAndSaveAbilities(any(Map.class), any(Analysis.class), any(User.class));

        // When
        Analysis response = analysisService.createAnalysis(record, user);

        // Then
        verify(analysisAIService).generateMemoSummary(testContent);
        verify(analysisAIService).generateAbilityAnalysis(testContent);
        verify(analysisDbService).saveAnalysis(any(Analysis.class));

        assertEquals(response.getContent(), testContent);
        assertEquals(response.getComment(), testComment);
    }

    @Test
    @DisplayName("메모 역량 분석 요약 글자수 예외 발생 테스트")
    void createMemoAnalysisWithNotEnoughContentTest() {
        // Given
        String overContent = "Test".repeat(500);
        when(analysisAIService.generateMemoSummary(any(String.class))).thenReturn(overContent);

        // When & Then
        AnalysisException exception = assertThrows(AnalysisException.class,
                () -> analysisService.createAnalysis(record, user));
        assertEquals(exception.getErrorStatus(), AnalysisErrorStatus.OVERFLOW_ANALYSIS_CONTENT);
    }

    @Test
    @DisplayName("메모 역량 분석 코멘트 글자수 예외 발생 테스트")
    void createMemoAnalysisWithNotEnoughCommentTest() {
        // Given
        String overComment = "Test".repeat(200);
        when(analysisAIService.generateMemoSummary(any(String.class))).thenReturn(testContent);
        when(analysisAIService.generateAbilityAnalysis(any(String.class)))
                .thenReturn(new AnalysisAiResponse(Map.of("커뮤니케이션", "Test Keyword Content"), overComment));

        // When & Then
        AnalysisException exception = assertThrows(AnalysisException.class,
                () -> analysisService.createAnalysis(record, user));
        assertEquals(exception.getErrorStatus(), AnalysisErrorStatus.OVERFLOW_ANALYSIS_COMMENT);
    }

    @Test
    @DisplayName("메모 역량 분석 키워드 글자수 예외 발생 테스트")
    void createMemoAnalysisWithLongKeywordCommentTest() {
        // Given
        String overKeywordComment = "Test".repeat(200);
        when(analysisAIService.generateMemoSummary(any(String.class))).thenReturn(testContent);
        when(analysisAIService.generateAbilityAnalysis(any(String.class)))
                .thenReturn(new AnalysisAiResponse(Map.of("커뮤니케이션", overKeywordComment), testComment));

        // When & Then
        AnalysisException exception = assertThrows(AnalysisException.class,
                () -> analysisService.createAnalysis(record, user));
        assertEquals(exception.getErrorStatus(), AnalysisErrorStatus.OVERFLOW_ANALYSIS_KEYWORD_CONTENT);
    }

    @Test
    @DisplayName("역량 분석 수정 테스트")
    void updateAnalysisTest() {
        // Given
        Ability ability = createMockAbility(analysis);
        analysis.addAbility(ability);

        when(userDbService.findUserById(1L)).thenReturn(user);
        when(analysisDbService.findAnalysisById(1L)).thenReturn(analysis);
        doAnswer(invocation -> {
            Record record = invocation.getArgument(0);
            String title = invocation.getArgument(1);
            record.updateTitle(title);
            return null;
        }).when(recordDbService).updateRecordTitle(any(Record.class), anyString());

        doAnswer(invocation -> {
            Analysis analysis = invocation.getArgument(0);
            String content = invocation.getArgument(1);
            analysis.updateContent(content);
            return null;
        }).when(analysisDbService).updateAnalysisContent(any(Analysis.class), anyString());

        doAnswer(invocation -> {
            Analysis passedAnalysis = invocation.getArgument(0); // 첫 번째 인수: Analysis
            Map<String, String> passedAbilityMap = invocation.getArgument(1); // 두 번째 인수: abilityMap

            passedAbilityMap.forEach((keyword, content) -> {
                Keyword key = Keyword.getName(keyword);
                Ability matchedAbility = passedAnalysis.getAbilityList().stream()
                        .filter(a -> a.getKeyword().equals(key))
                        .findFirst()
                        .orElseThrow(() -> new AbilityException(AbilityErrorStatus.INVALID_KEYWORD));
                matchedAbility.updateContent(content);
            });
            return null;
        }).when(abilityService).updateAbilityContents(any(Analysis.class), any(Map.class));

        // When
        Map<String, String> abilityMap = Map.of("커뮤니케이션", "Updated Keyword Content");
        AnalysisRequest.AnalysisUpdateDto request = AnalysisRequest.AnalysisUpdateDto.builder()
                .analysisId(1L)
                .title("Updated Title")
                .content("Updated Content".repeat(5))
                .abilityMap(abilityMap)
                .build();

        AnalysisResponse.AnalysisDto response = analysisService.updateAnalysis(1L, request);

        // Then
        verify(userDbService, times(1)).findUserById(1L);
        verify(analysisDbService, times(1)).findAnalysisById(1L);
        verify(recordDbService, times(1)).updateRecordTitle(record, "Updated Title");
        verify(analysisDbService, times(1)).updateAnalysisContent(analysis, "Updated Content".repeat(5));
        verify(abilityService, times(1)).updateAbilityContents(analysis, abilityMap);

        assertEquals(response.getAnalysisId(), analysis.getAnalysisId());
        assertEquals(response.getRecordId(), record.getRecordId());
        assertEquals(response.getRecordTitle(), "Updated Title");
        assertEquals(response.getRecordContent(), "Updated Content".repeat(5));
        assertEquals(response.getComment(), analysis.getComment());
        assertEquals(response.getAbilityDtoList().get(0).getKeyword(), "커뮤니케이션");
        assertEquals(response.getAbilityDtoList().get(0).getContent(), "Updated Keyword Content");
    }

    @Test
    @DisplayName("역량 분석 수정 중 존재하지 않는 키워드 제시 시 예외 발생 테스트")
    void findAbilityByNotExistingKeyword() {
        // Given
        Map<String, String> abilityMap = Map.of("협동", testContent);
        Ability ability = createMockAbility(analysis);
        analysis.addAbility(ability);

        when(userDbService.findUserById(1L)).thenReturn(user);
        when(analysisDbService.findAnalysisById(1L)).thenReturn(analysis);
        doThrow(new AbilityException(AbilityErrorStatus.INVALID_KEYWORD))
                .when(abilityService).updateAbilityContents(any(Analysis.class), any(Map.class));

        // When & Then
        AnalysisRequest.AnalysisUpdateDto request = AnalysisRequest.AnalysisUpdateDto.builder()
                .analysisId(1L)
                .abilityMap(abilityMap)
                .build();

        AbilityException exception = assertThrows(AbilityException.class,
                () -> analysisService.updateAnalysis(1L, request));

        assertEquals(exception.getErrorStatus(), AbilityErrorStatus.INVALID_KEYWORD);
        verify(userDbService, times(1)).findUserById(1L);
        verify(analysisDbService, times(1)).findAnalysisById(1L);
        verify(abilityService, times(1)).updateAbilityContents(analysis, abilityMap);
    }

    @Test
    @DisplayName("역량 분석 상세 정보 조회 테스트")
    void getAnalysisDetailTest() {
        // Given
        Ability ability = createMockAbility(analysis);
        analysis.addAbility(ability);

        when(userDbService.findUserById(1L)).thenReturn(user);
        when(analysisDbService.findAnalysisById(1L)).thenReturn(analysis);

        // When
        AnalysisResponse.AnalysisDto response = analysisService.getAnalysis(1L, 1L);

        // Then
        verify(userDbService, times(1)).findUserById(1L);
        verify(analysisDbService, times(1)).findAnalysisById(1L);

        assertEquals(response.getAnalysisId(), analysis.getAnalysisId());
        assertEquals(response.getRecordId(), record.getRecordId());
        assertEquals(response.getRecordTitle(), testTitle);
        assertEquals(response.getRecordContent(), testContent);
        assertEquals(response.getComment(), testComment);
        assertEquals(response.getAbilityDtoList().get(0).getKeyword(), "커뮤니케이션");
        assertEquals(response.getAbilityDtoList().get(0).getContent(), "Test Keyword Content");
    }

    @Test
    @DisplayName("역량 분석 삭제 테스트")
    void deleteAnalysisTest() {
        // Given
        Ability ability = createMockAbility(analysis);
        analysis.addAbility(ability);

        when(userDbService.findUserById(1L)).thenReturn(user);
        when(analysisDbService.findAnalysisById(1L)).thenReturn(analysis);

        // When
        analysisService.deleteAnalysis(1L, 1L);

        // Then
        verify(userDbService, times(1)).findUserById(1L);
        verify(analysisDbService, times(1)).findAnalysisById(1L);
        verify(analysisDbService).deleteAnalysis(analysis);
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
                .abilityList(new ArrayList<>())
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
