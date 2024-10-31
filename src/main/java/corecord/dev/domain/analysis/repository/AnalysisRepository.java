package corecord.dev.domain.analysis.repository;

import corecord.dev.domain.analysis.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    @Query("SELECT a " +
            "FROM Analysis a " +
            "JOIN FETCH a.record r " +
            "JOIN FETCH a.abilityList al " +
            "WHERE a.analysisId = :id")
    Optional<Analysis> findAnalysisById(@Param(value = "id") Long id);
}
