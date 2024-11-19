package corecord.dev.analysis.service;

import corecord.dev.domain.ability.domain.entity.Ability;
import corecord.dev.domain.ability.domain.entity.Keyword;
import corecord.dev.domain.ability.status.AbilityErrorStatus;
import corecord.dev.domain.ability.exception.AbilityException;
import corecord.dev.domain.ability.application.AbilityService;
import corecord.dev.domain.analysis.domain.dto.request.AnalysisRequest;
import corecord.dev.domain.analysis.infra.openai.dto.response.AnalysisAiResponse;
import corecord.dev.domain.analysis.domain.dto.response.AnalysisResponse;
import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.analysis.status.AnalysisErrorStatus;
import corecord.dev.domain.analysis.exception.AnalysisException;
import corecord.dev.domain.analysis.domain.repository.AnalysisRepository;
import corecord.dev.domain.analysis.application.AnalysisService;
import corecord.dev.domain.analysis.infra.openai.application.OpenAiService;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.record.domain.entity.RecordType;
import corecord.dev.domain.record.domain.entity.Record;
import corecord.dev.domain.user.domain.entity.Status;
import corecord.dev.domain.user.domain.entity.User;
import corecord.dev.domain.user.domain.repository.UserRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class AnalysisServiceTest {

    @Mock
    private UserRepository userRepository;

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
    @DisplayName("역량 분석 수정 테스트")
    void updateAnalysisTest() {
        // Given
        Ability ability = createMockAbility(analysis);
        analysis.addAbility(ability);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(analysisRepository.findAnalysisById(1L)).thenReturn(Optional.of(analysis));

        // When
        AnalysisRequest.AnalysisUpdateDto request = AnalysisRequest.AnalysisUpdateDto.builder()
                .analysisId(1L)
                .title("Updated Title")
                .content("Updated Content".repeat(5))
                .abilityMap(Map.of("커뮤니케이션", "Updated Keyword Content"))
                .build();

        AnalysisResponse.AnalysisDto response = analysisService.updateAnalysis(1L, request);

        // Then
        verify(userRepository, times(1)).findById(1L);
        verify(analysisRepository, times(1)).findAnalysisById(1L);

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
        Ability ability = createMockAbility(analysis);
        analysis.addAbility(ability);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(analysisRepository.findAnalysisById(1L)).thenReturn(Optional.of(analysis));

        // When & Then
        AnalysisRequest.AnalysisUpdateDto request = AnalysisRequest.AnalysisUpdateDto.builder()
                .analysisId(1L)
                .abilityMap(Map.of("협동", testContent))
                .build();

        AbilityException exception = assertThrows(AbilityException.class,
                () -> analysisService.updateAnalysis(1L, request));

        assertEquals(exception.getAbilityErrorStatus(), AbilityErrorStatus.INVALID_KEYWORD);
        verify(userRepository, times(1)).findById(1L);
        verify(analysisRepository, times(1)).findAnalysisById(1L);
    }

    @Test
    @DisplayName("역량 분석 상세 정보 조회 테스트")
    void getAnalysisDetailTest() {
        // Given
        Ability ability = createMockAbility(analysis);
        analysis.addAbility(ability);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(analysisRepository.findAnalysisById(1L)).thenReturn(Optional.of(analysis));

        // When
        AnalysisResponse.AnalysisDto response = analysisService.getAnalysis(1L, 1L);

        // Then
        verify(userRepository, times(1)).findById(1L);
        verify(analysisRepository, times(1)).findAnalysisById(1L);

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

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(analysisRepository.findAnalysisById(1L)).thenReturn(Optional.of(analysis));

        // When
        analysisService.deleteAnalysis(1L, 1L);

        // Then
        verify(userRepository, times(1)).findById(1L);
        verify(analysisRepository, times(1)).findAnalysisById(1L);
        verify(analysisRepository).delete(analysis);
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
