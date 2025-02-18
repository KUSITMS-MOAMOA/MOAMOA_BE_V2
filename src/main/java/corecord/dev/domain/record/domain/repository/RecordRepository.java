package corecord.dev.domain.record.domain.repository;

import corecord.dev.domain.ability.domain.enums.Keyword;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.record.domain.entity.Record;
import corecord.dev.domain.user.domain.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
            "JOIN r.user u " +
            "WHERE u.userId = :userId " +
            "AND (:last_record_id = 0 OR r.recordId < :last_record_id) " +  // 제일 마지막에 읽은 데이터 이후부터 가져옴
            "AND r.folder is not null AND r.folder = :folder") // 임시 저장 기록 제외
    List<Record> findRecordsByFolder(
            @Param(value = "folder") Folder folder,
            @Param(value = "userId") Long userId,
            @Param(value = "last_record_id") Long lastRecordId,
            Pageable pageable);

    @Query("SELECT r FROM Record r " +
            "JOIN FETCH r.analysis a " +
            "JOIN FETCH r.folder f " +
            "JOIN FETCH a.abilityList al " +
            "JOIN r.user u " +
            "WHERE u.userId = :userId " +
            "AND (:last_record_id <= 0 OR r.recordId < :last_record_id) " + // 제일 마지막에 읽은 데이터 이후부터 가져옴
            "AND r.folder is not null") // 임시 저장 기록 제외
    List<Record> findRecords(
            @Param(value = "userId") Long userId,
            @Param(value = "last_record_id") Long lastRecordId,
            Pageable pageable);


    @Query("SELECT r FROM Ability a " +
            "JOIN a.analysis an " +
            "JOIN an.record r " +
            "JOIN FETCH r.folder f " +
            "WHERE a.user.userId = :userId " +
            "AND a.keyword = :keyword " +
            "AND (:last_record_id = 0 OR r.recordId < :last_record_id) " + // 제일 마지막에 읽은 데이터 이후부터 가져옴
            "AND r.folder is not null") // 임시 저장 기록 제외
    List<Record> findRecordsByKeyword(
            @Param(value = "keyword")Keyword keyword,
            @Param(value = "userId") Long userId,
            @Param(value = "last_record_id") Long lastRecordId,
            Pageable pageable
            );

    @Query("SELECT r FROM Record r " +
            "JOIN FETCH r.analysis a " +
            "JOIN FETCH r.folder f " +
            "WHERE r.recordId = :id")
    Optional<Record> findRecordById(@Param(value = "id") Long id);

    @Query("SELECT COUNT(r) " +
            "FROM Record r " +
            "WHERE r.user = :user " +
            "AND r.folder is not null") // 임시 저장 기록 제외
    int getRecordCount(@Param(value = "user") User user);

    @Modifying
    @Query("DELETE " +
            "FROM Record r " +
            "WHERE r.user.userId IN :userId")
    void deleteRecordByUserId(@Param(value = "userId") Long userId);

    @Modifying
    @Query("DELETE " +
            "FROM Record r " +
            "WHERE r.folder = :folder")
    void deleteRecordByFolder(@Param(value = "folder") Folder folder);
}
