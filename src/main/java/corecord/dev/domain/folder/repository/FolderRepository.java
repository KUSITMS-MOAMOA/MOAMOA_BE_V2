package corecord.dev.domain.folder.repository;

import corecord.dev.domain.folder.dto.response.FolderResponse;
import corecord.dev.domain.folder.entity.Folder;
import corecord.dev.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    @Query("SELECT new corecord.dev.domain.folder.dto.response.FolderResponse$FolderDto(f.folderId, f.title) " +
            "FROM Folder f " +
            "WHERE f.user = :user " +
            "ORDER BY f.createdAt desc ")
    List<FolderResponse.FolderDto> findFolderDtoList(@Param(value = "user") User user);

    @Query("SELECT f " +
            "FROM Folder f " +
            "WHERE f.title = :title AND f.user = :user ")
    Optional<Folder> findFolderByTitle(
            @Param(value = "title") String title,
            @Param(value = "user") User user);

    boolean existsByTitle(String title);
}
