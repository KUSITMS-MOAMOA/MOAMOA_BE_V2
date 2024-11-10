package corecord.dev.domain.user.repository;

import corecord.dev.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderId(String providerId);
    boolean existsByProviderId(String providerId);

    @Modifying
    @Query("DELETE FROM User u " +
            "WHERE u.userId = :userId")
    void deleteUserByUserId(@Param(value = "userId") Long userId);
}
