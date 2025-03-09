package corecord.dev.domain.folder.domain.repository;

import corecord.dev.domain.folder.domain.dto.response.FolderResponse;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    @Query("SELECT new corecord.dev.domain.folder.domain.dto.response.FolderResponse$FolderDto(f.folderId, f.title) " +
            "FROM Folder f " +
            "WHERE f.user.userId = :userId " +
            "AND f.title <> '경험 기록 예시 폴더' " +
            "ORDER BY f.createdAt desc ")
    List<FolderResponse.FolderDto> findFolderDtoList(@Param(value = "userId") Long userId);

    @Query("SELECT f " +
            "FROM Folder f " +
            "JOIN FETCH f.user u " +
            "WHERE f.title = :title AND u.userId = :userId ")
    Optional<Folder> findFolderByTitle(
            @Param(value = "title") String title,
            @Param(value = "userId") Long userId);

    boolean existsByTitleAndUser(String title, User user);

    @Modifying
    @Query("DELETE " +
            "FROM Folder f " +
            "WHERE f.user.userId IN :userId")
    void deleteFolderByUserId(@Param(value = "userId") Long userId);


    @Modifying
    @Query("DELETE " +
            "FROM Folder f " +
            "WHERE f = :folder")
    void delete(@Param(value = "folder") Folder folder);
}
