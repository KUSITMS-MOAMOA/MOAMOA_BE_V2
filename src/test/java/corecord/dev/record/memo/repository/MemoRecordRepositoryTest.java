package corecord.dev.record.memo.repository;

import corecord.dev.domain.folder.entity.Folder;
import corecord.dev.domain.record.constant.RecordType;
import corecord.dev.domain.record.entity.Record;
import corecord.dev.domain.record.repository.RecordRepository;
import corecord.dev.domain.user.entity.Status;
import corecord.dev.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MemoRecordRepositoryTest {
    @Autowired
    EntityManager entityManager;

    @Autowired
    RecordRepository recordRepository;

    private final Long lastRecordId = 0L;
    private final Pageable pageable = PageRequest.of(0, 5);
    private final String testTitle = "Test Title";
    private final String testContent = "Test Content";


    @Test
    void findRecordByFolder() {
        // Given
        User user = createUser("Test User");
        entityManager.persist(user);

        Folder folder = createFolder("Test Folder", user);
        entityManager.persist(folder);

        Record record1 = createRecord("Test Record1", testContent, user, folder);
        entityManager.persist(record1);
        Record record2 = createRecord("Test Record2", testContent, user, folder);
        entityManager.persist(record2);

        // When
        List<Record> result = recordRepository.findRecordsByFolder(folder, user, lastRecordId, pageable);

        // Then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getRecordId()).isEqualTo(record1.getRecordId());
        assertThat(result.get(0).getFolder().getFolderId()).isEqualTo(folder.getFolderId());
        assertThat(result.get(1).getRecordId()).isEqualTo(record2.getRecordId());
        assertThat(result.get(1).getFolder().getFolderId()).isEqualTo(folder.getFolderId());
    }

    private User createUser(String nickName) {
        return User.builder()
                .providerId("Test Provider")
                .nickName(nickName)
                .status(Status.GRADUATE_STUDENT)
                .folders(new ArrayList<>())
                .build();
    }

    private Record createRecord(String title, String content, User user, Folder folder) {
        return Record.builder()
                .title(title)
                .content(content)
                .user(user)
                .type(RecordType.MEMO)
                .folder(folder)
                .build();
    }

    private Folder createFolder(String title, User user) {
        return Folder.builder()
                .title(title)
                .user(user)
                .build();
    }

}
