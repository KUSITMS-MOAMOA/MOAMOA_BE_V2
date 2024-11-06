package corecord.dev.domain.record.repository;

import corecord.dev.domain.analysis.constant.Keyword;
import corecord.dev.domain.folder.entity.Folder;
import corecord.dev.domain.record.entity.Record;
import corecord.dev.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {

    @Query("SELECT r " +
            "FROM Record r " +
            "JOIN FETCH r.analysis a " +
            "JOIN FETCH r.folder f " +
            "JOIN FETCH a.abilityList al " +
            "WHERE r.user = :user " +
            "AND r.folder is not null AND r.folder = :folder "+ // 임시 저장 기록 제외
            "ORDER BY r.createdAt desc ") // 최근 생성 순 정렬
    List<Record> findRecordsByFolder(
            @Param(value = "folder") Folder folder,
            @Param(value = "user") User user);

    @Query("SELECT r FROM Record r " +
            "JOIN FETCH r.analysis a " +
            "JOIN FETCH r.folder f " +
            "JOIN FETCH a.abilityList al " +
            "WHERE r.user = :user " +
            "AND r.folder is not null " + // 임시 저장 기록 제외
            "ORDER BY r.createdAt DESC") // 최근 생성 순 정렬
    List<Record> findRecords(@Param(value = "user") User user);


    @Query("SELECT r FROM Ability a " +
            "JOIN a.analysis an " +
            "JOIN an.record r " +
            "JOIN FETCH r.folder f " +
            "WHERE a.user = :user " +
            "AND a.keyword = :keyword " +
            "AND r.folder is not null " + // 임시 저장 기록 제외
            "ORDER BY r.createdAt DESC") // 최근 생성 순 정렬
    List<Record> findRecordsByKeyword(
            @Param(value = "keyword")Keyword keyword,
            @Param(value = "user") User user);

    @Query("SELECT r FROM Record r " +
            "JOIN FETCH r.analysis a " +
            "JOIN FETCH r.folder f " +
            "JOIN FETCH a.abilityList al " +
            "WHERE r.user = :user " +
            "AND r.folder is not null ")  // 임시 저장 기록 제외
    List<Record> findRecordsOrderByCreatedAt(
            @Param(value = "user") User user,
            Pageable pageable);

    @Query("SELECT r FROM Record r " +
            "JOIN FETCH r.analysis a " +
            "JOIN FETCH r.folder f " +
            "WHERE r.recordId = :id")
    Optional<Record> findRecordById(@Param(value = "id") Long id);

    @Query("SELECT COUNT(r) " +
            "FROM Record r " +
            "WHERE r.user = :user")
    int getRecordCount(@Param(value = "user") User user);
}
