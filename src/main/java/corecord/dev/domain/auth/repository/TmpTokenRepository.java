package corecord.dev.domain.auth.repository;

import corecord.dev.domain.auth.entity.TmpToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TmpTokenRepository extends CrudRepository<TmpToken, String> {
    Optional<TmpToken> findByTmpToken(String tmpToken);
}
