package corecord.dev.ability.service;

import corecord.dev.domain.ability.application.AbilityDbService;
import corecord.dev.domain.ability.domain.dto.response.AbilityResponse;
import corecord.dev.domain.ability.domain.entity.Ability;
import corecord.dev.domain.ability.domain.enums.Keyword;
import corecord.dev.domain.ability.status.AbilityErrorStatus;
import corecord.dev.domain.ability.exception.AbilityException;
import corecord.dev.domain.ability.application.AbilityService;
import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.record.domain.enums.RecordType;
import corecord.dev.domain.record.domain.entity.Record;
import corecord.dev.domain.user.application.UserDbService;
import corecord.dev.domain.user.domain.entity.enums.Status;
import corecord.dev.domain.user.domain.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AbilityServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private UserDbService userDbService;

    @Mock
    private AbilityDbService abilityDbService;

    @InjectMocks
    AbilityService abilityService;

    private User user;
    private Folder folder;
    private Record record;
    private Analysis analysis;
    private String testKeywordComment = "Test Keyword Comment";

    @BeforeEach
    void setUp() {
        user = createMockUser();
        folder = createMockFolder(user);
        record = createMockRecord(user, folder);
        analysis = createMockAnalysis(record);
        analysis.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("유저의 경험 키워드 리스트 조회 테스트")
    void getKeywordListTest() {
        // Given
        Ability ability1 = createMockAbility(Keyword.COMMUNICATION, analysis);
        Ability ability2 = createMockAbility(Keyword.LEADERSHIP, analysis);
        analysis.addAbility(ability1);
        analysis.addAbility(ability2);

        when(userDbService.findUserById(1L)).thenReturn(user);
        when(abilityDbService.findKeywordList(any(User.class)))
                .thenReturn(List.of(Keyword.COMMUNICATION.getValue(), Keyword.LEADERSHIP.getValue()));

        // When
        AbilityResponse.KeywordListDto response = abilityService.getKeywordList(1L);

        // Then
        verify(userDbService, times(1)).findUserById(1L);
        verify(abilityDbService, times(1)).findKeywordList(user);

        assertEquals(2, response.getKeywordList().size());
        assertEquals(Keyword.COMMUNICATION.getValue(), response.getKeywordList().get(0));
        assertEquals(Keyword.LEADERSHIP.getValue(), response.getKeywordList().get(1));
    }

    @Test
    @DisplayName("경험 키워드 파싱 테스트")
    void parseAndSaveAbilitiesTest() {
        // Given
        Map<String, String> keywordList = Map.of(
                Keyword.COMMUNICATION.getValue(), testKeywordComment,
                Keyword.LEADERSHIP.getValue(), testKeywordComment);

        // When
        abilityService.parseAndSaveAbilities(keywordList, analysis, user);

        // Then
        verify(abilityDbService, times(2)).saveAbility(any(Ability.class));
        assertEquals(2, analysis.getAbilityList().size());
        assertEquals(Keyword.COMMUNICATION.getValue(), analysis.getAbilityList().get(0).getKeyword().getValue());
        assertEquals(Keyword.LEADERSHIP.getValue(), analysis.getAbilityList().get(1).getKeyword().getValue());
    }

    @Test
    @DisplayName("경험 키워드 파싱 결과의 개수가 0인 경우 테스트")
    void parseAbilityWithEmptyKeywordList() {
        // Given
        Map<String, String> keywordList = Map.of("Keyword", testKeywordComment);

        // When & Then
        AbilityException exception = assertThrows(AbilityException.class,
                () -> abilityService.parseAndSaveAbilities(keywordList, analysis, user));
        assertEquals(exception.getErrorStatus(), AbilityErrorStatus.INVALID_ABILITY_KEYWORD);
    }

    @Test
    void deleteOriginAbilityTest() {
        // Given
        Ability ability1 = createMockAbility(Keyword.COMMUNICATION, analysis);
        Ability ability2 = createMockAbility(Keyword.LEADERSHIP, analysis);
        analysis.addAbility(ability1);
        analysis.addAbility(ability2);

        assertEquals(2, analysis.getAbilityList().size());

        // When
        abilityService.deleteOriginAbilityList(analysis);

        // Then
        assertEquals(0, analysis.getAbilityList().size());
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
                .title("Test Record")
                .content("Test".repeat(10))
                .user(user)
                .type(RecordType.MEMO)
                .folder(folder)
                .build();
    }

    private Analysis createMockAnalysis(Record record) {
        return Analysis.builder()
                .analysisId(1L)
                .content("Test".repeat(10))
                .comment("Test Comment")
                .record(record)
                .abilityList(new ArrayList<>())
                .build();
    }

    private Ability createMockAbility(Keyword keyword, Analysis analysis) {
        return Ability.builder()
                .keyword(keyword)
                .content("Test Keyword Content")
                .user(user)
                .analysis(analysis)
                .build();
    }
}
