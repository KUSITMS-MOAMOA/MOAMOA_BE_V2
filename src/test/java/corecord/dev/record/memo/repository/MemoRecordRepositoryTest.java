package corecord.dev.record.memo.repository;

import corecord.dev.domain.ability.domain.entity.Ability;
import corecord.dev.domain.ability.domain.entity.Keyword;
import corecord.dev.domain.ability.domain.repository.AbilityRepository;
import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.analysis.domain.repository.AnalysisRepository;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.folder.domain.repository.FolderRepository;
import corecord.dev.domain.record.domain.entity.RecordType;
import corecord.dev.domain.record.domain.entity.Record;
import corecord.dev.domain.record.domain.repository.RecordRepository;
import corecord.dev.domain.user.domain.entity.Status;
import corecord.dev.domain.user.domain.entity.User;
import corecord.dev.domain.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MemoRecordRepositoryTest {

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private AbilityRepository abilityRepository;

    private final Long lastRecordId = 0L;
    private final Pageable pageable = PageRequest.of(0, 5);
    private final String testContent = "Test Content";


    @Test
    @DisplayName("폴더별 경험 기록 리스트 조회 테스트")
    void findRecordByFolder() {
        // Given
        User user = createUser("Test User");
        Folder folder = createFolder("Test Folder", user);

        Record record1 = createRecord("Test Record1", user, folder);
        Record record2 = createRecord("Test Record2", user, folder);

        // When
        List<Record> result = recordRepository.findRecordsByFolder(folder, user, lastRecordId, pageable);

        // Then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getRecordId()).isEqualTo(record1.getRecordId());
        assertThat(result.get(0).getFolder().getFolderId()).isEqualTo(folder.getFolderId());
        assertThat(result.get(1).getRecordId()).isEqualTo(record2.getRecordId());
        assertThat(result.get(1).getFolder().getFolderId()).isEqualTo(folder.getFolderId());
    }

    @Test
    @DisplayName("경험 기록이 존재하지 않는 폴더에 대한 리스트 조회 테스트")
    void findRecordByFolderWhenNoRecordsExist() {
        // Given
        User user = createUser("Test User");
        Folder folder = createFolder("Test Folder", user);

        // When
        List<Record> result = recordRepository.findRecordsByFolder(folder, user, lastRecordId, pageable);

        // Then
        assertEquals(result.size(), 0);
    }

    @Test
    @DisplayName("메모 경험 기록 조회 테스트")
    void findMemoRecordDetail() {
        // Given
        User user = createUser("Test User");
        Folder folder = createFolder("Test folder", user);
        Record record = createRecord("Test Record", user, folder);

        // When
        Optional<Record> result = recordRepository.findRecordById(record.getRecordId());

        // Then
        assertTrue(result.isPresent());
        assertThat(result.get().getTitle()).isEqualTo("Test Record");
        assertThat(result.get().getRecordId()).isEqualTo(record.getRecordId());
    }

    @Test
    @DisplayName("키워드별 경험 기록 조회 테스트")
    void findMemoRecordListByKeywordTest() {
        // Given
        User user = createUser("Test User");
        Folder folder = createFolder("Test folder", user);

        Record record1 = createRecord("Test Record1", user, folder);
        Record record2 = createRecord("Test Record2", user, folder);

        // When
        List<Record> result = recordRepository.findRecordsByKeyword(Keyword.COLLABORATION, user, lastRecordId, pageable);

        // Then
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getTitle(), record1.getTitle());
        assertEquals(result.get(1).getTitle(), record2.getTitle());
    }

    private User createUser(String nickName) {
        User user = User.builder()
                .providerId("Test Provider")
                .nickName(nickName)
                .status(Status.GRADUATE_STUDENT)
                .folders(new ArrayList<>())
                .build();
        userRepository.save(user);
        return user;
    }

    private Record createRecord(String title, User user, Folder folder) {
        Record record = Record.builder()
                .title(title)
                .content(testContent)
                .user(user)
                .type(RecordType.MEMO)
                .folder(folder)
                .build();
        recordRepository.save(record);
        createAnalysis(record, user);
        return record;
    }

    private Folder createFolder(String title, User user) {
        Folder folder = Folder.builder()
                .title(title)
                .user(user)
                .build();
        folderRepository.save(folder);
        return folder;
    }

    private Analysis createAnalysis(Record record, User user) {
        Analysis analysis = Analysis.builder()
                .content(testContent)
                .comment(testContent)
                .record(record)
                .build();
        analysisRepository.save(analysis);
        createAbility(user, analysis, Keyword.COLLABORATION);
        return analysis;
    }

    private Ability createAbility(User user, Analysis analysis, Keyword keyword) {
        Ability ability = Ability.builder()
                .keyword(keyword)
                .content(testContent)
                .user(user)
                .analysis(analysis)
                .build();
        abilityRepository.save(ability);
        return ability;
    }

}
