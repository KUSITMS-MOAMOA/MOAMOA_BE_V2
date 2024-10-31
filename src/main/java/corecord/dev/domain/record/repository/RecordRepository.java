package corecord.dev.domain.record.repository;

import corecord.dev.domain.folder.entity.Folder;
import corecord.dev.domain.record.entity.Record;
import corecord.dev.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
