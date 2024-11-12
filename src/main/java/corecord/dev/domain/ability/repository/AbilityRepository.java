package corecord.dev.domain.ability.repository;

import corecord.dev.domain.ability.dto.response.AbilityResponse;
import corecord.dev.domain.ability.entity.Ability;
import corecord.dev.domain.ability.entity.Keyword;
import corecord.dev.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbilityRepository extends JpaRepository<Ability, Long> {
        @Query("SELECT new corecord.dev.domain.ability.dto.response.AbilityResponse$KeywordStateDto(" +
                "a.keyword, COUNT(a), " + // 각 키워드의 개수 집계
                "(COUNT(a) * 1.0 / (SELECT COUNT(a2) FROM Ability a2 WHERE a2.user = :user)) * 100.0) " + // 각 키워드의 비율 집계
                "FROM Ability a " +
                "WHERE a.user = :user " +
                "GROUP BY a.keyword " +
                "ORDER BY 3 desc ") // 비율 높은 순 정렬
        List<AbilityResponse.KeywordStateDto> findKeywordStateDtoList(@Param(value = "user") User user);

        @Modifying
        @Query("DELETE " +
                "FROM Ability a " +
                "WHERE a.user.userId IN :userId")
        void deleteAbilityByUserId(@Param(value = "userId") Long userId);

        @Query("SELECT distinct a.keyword AS keyword " + // unique한 keyword list 반환
                "FROM Ability a " +
                "JOIN a.analysis ana " +
                "WHERE a.user = :user " +
                "GROUP BY a.keyword " +
                "ORDER BY COUNT(a.keyword) DESC, MAX(ana.createdAt) DESC ") // 개수가 많은 순, 최근 생성 순 정렬
        List<Keyword> getKeywordList(@Param(value = "user") User user);
}
