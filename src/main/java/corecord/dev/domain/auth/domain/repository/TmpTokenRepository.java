package corecord.dev.domain.auth.domain.repository;

import corecord.dev.domain.auth.domain.entity.TmpToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TmpTokenRepository extends CrudRepository<TmpToken, String> {
    Optional<TmpToken> findByTmpToken(String tmpToken);
}
