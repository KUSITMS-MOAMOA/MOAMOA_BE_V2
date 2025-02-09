package corecord.dev.folder.repository;

import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.folder.domain.repository.FolderRepository;
import corecord.dev.domain.user.domain.entity.Status;
import corecord.dev.domain.user.domain.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FolderRepositoryTest {
    @Autowired
    EntityManager entityManager;
    @Autowired
    FolderRepository folderRepository;

    @Test
    void findFolderByTitle() {
        String testTitle1 = "Test Title1";
        String testTitle2 = "Test Title2";

        // Given
        User user = createUser("Test User");
        entityManager.persist(user);

        Folder folder1 = createFolder(testTitle1, user);
        entityManager.persist(folder1);

        Folder folder2 = createFolder(testTitle2, user);
        entityManager.persist(folder2);

        Long testId = folder2.getFolderId();

        // When
        Optional<Folder> result = folderRepository.findFolderByTitle(testTitle2, user.getUserId());

        // Then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getUser()).isEqualTo(user);
        assertThat(result.get().getFolderId()).isEqualTo(testId);
        assertThat(result.get().getTitle()).isEqualTo(testTitle2);
    }

    @Test
    void existByTitle() {
        String testTitle = "Test Title";

        // Given
        User user = createUser("Test User");
        entityManager.persist(user);

        Folder folder1 = createFolder(testTitle, user);
        entityManager.persist(folder1);

        // When
        boolean result = folderRepository.existsByTitleAndUser(testTitle, user);

        // Then
        assertThat(result).isEqualTo(true);
    }

    private User createUser(String nickName) {
        return User.builder()
                .providerId("Test Provider")
                .nickName(nickName)
                .status(Status.GRADUATE_STUDENT)
                .folders(new ArrayList<>())
                .build();
    }

    private Folder createFolder(String title, User user) {
        return Folder.builder()
                .title(title)
                .user(user)
                .build();
    }
}
