package corecord.dev.domain.analysis.repository;

import corecord.dev.domain.analysis.constant.Keyword;
import corecord.dev.domain.analysis.entity.Analysis;
import corecord.dev.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    @Query("SELECT a " +
            "FROM Analysis a " +
            "JOIN FETCH a.record r " +
            "JOIN FETCH a.abilityList al " +
            "WHERE a.analysisId = :id")
    Optional<Analysis> findAnalysisById(@Param(value = "id") Long id);

    @Query("SELECT distinct a.keyword AS keyword " + // unique한 keyword list 반환
            "FROM Ability a " +
            "JOIN a.analysis ana " +
            "WHERE a.user = :user " +
            "GROUP BY a.keyword " +
            "ORDER BY COUNT(a.keyword) DESC, MAX(ana.createdAt) DESC ") // 개수가 많은 순, 최근 생성 순 정렬
    List<Keyword> getKeywordList(@Param(value = "user") User user);
}
