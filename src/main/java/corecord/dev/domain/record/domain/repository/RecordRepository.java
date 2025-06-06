package corecord.dev.domain.record.domain.repository;

import corecord.dev.domain.ability.domain.enums.Keyword;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.record.domain.entity.Record;
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

    @Query("SELECT r.recordId FROM Record r " +
            "JOIN r.user u " +
            "WHERE u.userId = :userId " +
            "AND (:last_record_id = 0 OR r.recordId < :last_record_id) " +  // 제일 마지막에 읽은 데이터 이후부터 가져옴
            "AND r.folder is not null " + // 임시 저장 기록 제외
            "AND r.folder = :folder " +
            "AND r.folder.title <> :example_folder_name") // 예시 경험 기록 제외
    List<Long> findRecordIdsByFolder(
            @Param(value = "folder") Folder folder,
            @Param(value = "userId") Long userId,
            @Param(value = "last_record_id") Long lastRecordId,
            @Param(value = "example_folder_name") String exampleFolderName,
            Pageable pageable);

    @Query("SELECT r.recordId FROM Record r " +
            "JOIN r.user u " +
            "WHERE u.userId = :userId " +
            "AND (:last_record_id <= 0 OR r.recordId < :last_record_id) " + // 제일 마지막에 읽은 데이터 이후부터 가져옴
            "AND r.folder is not null " + // 임시 저장 기록 제외
            "AND r.folder.title <> :example_folder_name") // 예시 경험 기록 제외
    List<Long> findRecordIds(
            @Param(value = "userId") Long userId,
            @Param(value = "last_record_id") Long lastRecordId,
            @Param(value = "example_folder_name") String exampleFolderName,
            Pageable pageable);

    @Query("SELECT r.recordId FROM Record r " +
            "JOIN r.user u " +
            "WHERE u.userId = :userId " +
            "AND r.folder is not null") // 임시 저장 기록 제외
    List<Long> findRecentRecordIds(
            @Param(value = "userId") Long userId,
            Pageable pageable);

    @Query("SELECT r FROM Ability a " +
            "JOIN a.analysis.record r " +
            "JOIN FETCH r.folder f " +
            "WHERE a.user.userId = :userId " +
            "AND a.keyword = :keyword " +
            "AND (:last_record_id = 0 OR r.recordId < :last_record_id) " + // 제일 마지막에 읽은 데이터 이후부터 가져옴
            "AND r.folder is not null " + // 임시 저장 기록 제외
            "AND r.folder.title NOT IN (:example_folder_name)") // 예시 경험 기록 제외
    List<Record> findRecordsByKeyword(
            @Param(value = "keyword")Keyword keyword,
            @Param(value = "userId") Long userId,
            @Param(value = "last_record_id") Long lastRecordId,
            @Param(value = "example_folder_name") String exampleFolderName,
            Pageable pageable
            );

    @Query("SELECT r FROM Record r " +
            "JOIN FETCH r.analysis a " +
            "JOIN FETCH r.folder f " +
            "JOIN FETCH a.abilityList al " +
            "JOIN FETCH r.user u " +
            "WHERE r.recordId IN :record_ids " +
            "ORDER BY r.createdAt DESC")
    List<Record> findRecordsByIds(@Param("record_ids") List<Long> recordIds);

    @Query("SELECT r FROM Record r " +
            "JOIN FETCH r.analysis a " +
            "JOIN FETCH r.folder f " +
            "WHERE r.recordId = :id")
    Optional<Record> findRecordById(@Param(value = "id") Long id);

    @Query("SELECT COUNT(r) " +
            "FROM Record r " +
            "WHERE r.user.userId = :userId " +
            "AND r.folder is not null " + // 임시 저장 기록 제외
            "AND r.folder.title <> :example_folder_name") // 예시 기록 제외
    int getRecordCount(@Param(value = "userId") Long userId,
                       @Param(value = "example_folder_name") String exampleFolderName);

    @Query("SELECT COUNT(r) " +
            "FROM Record r " +
            "WHERE r.user.userId = :userId " +
            "AND r.folder is not null " + // 임시 저장 기록 제외
            "AND r.folder.title <> :example_folder_name " + // 예시 기록 제외
            "AND r.type = 'CHAT' ")
    int getRecordCountByType(@Param(value = "userId") Long userId,
                             @Param(value = "example_folder_name") String exampleFolderName);

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
