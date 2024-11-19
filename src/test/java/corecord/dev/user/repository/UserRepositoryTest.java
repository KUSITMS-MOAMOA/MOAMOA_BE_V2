package corecord.dev.user.repository;

import corecord.dev.domain.user.domain.entity.Status;
import corecord.dev.domain.user.domain.entity.User;
import corecord.dev.domain.user.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("UserId로 회원 삭제")
    void deleteUserByUserId() {
        // Given
        User user = createTestUser();
        userRepository.save(user);

        // When
        userRepository.deleteUserByUserId(user.getUserId());
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<User> deletedUser = userRepository.findById(user.getUserId());
        assertThat(deletedUser).isEmpty();
    }


    private User createTestUser() {
        return User.builder()
                .userId(1L)
                .providerId("providerId")
                .nickName("testUser")
                .status(Status.UNIVERSITY_STUDENT)
                .abilities(new ArrayList<>())
                .chatRooms(new ArrayList<>())
                .folders(new ArrayList<>())
                .records(new ArrayList<>())
                .build();
    }
}
