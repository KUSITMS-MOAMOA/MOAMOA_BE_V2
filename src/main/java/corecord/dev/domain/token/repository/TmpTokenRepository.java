package corecord.dev.domain.token.repository;

import corecord.dev.domain.token.entity.TmpToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TmpTokenRepository extends CrudRepository<TmpToken, String> {
    Optional<TmpToken> findByTmpToken(String tmpToken);
}
