package corecord.dev.domain.token.repository;

import corecord.dev.domain.token.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    void deleteByUserId(Long userId);
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
