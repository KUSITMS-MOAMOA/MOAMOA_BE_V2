package corecord.dev.domain.analysis.domain.repository;

import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.folder.domain.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    @Query("SELECT a " +
            "FROM Analysis a " +
            "JOIN FETCH a.record r " +
            "JOIN FETCH r.folder f " +
            "JOIN FETCH a.abilityList al " +
            "JOIN FETCH r.user u " +
            "WHERE a.analysisId = :id")
    Optional<Analysis> findAnalysisById(@Param(value = "id") Long id);

    @Modifying
    @Query("DELETE " +
            "FROM Analysis a " +
            "WHERE a.record.user.userId IN :userId")
    void deleteAnalysisByUserId(@Param(value = "userId") Long userId);

    @Modifying
    @Query("DELETE " +
            "FROM Analysis a " +
            "WHERE a.record.folder = :folder")
    void deleteAnalysisByFolder(@Param(value = "folder") Folder folder);
}
