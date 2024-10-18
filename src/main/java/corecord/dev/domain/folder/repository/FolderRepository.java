package corecord.dev.domain.folder.repository;

import corecord.dev.domain.folder.dto.response.FolderResponse;
import corecord.dev.domain.folder.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    @Query("SELECT new corecord.dev.domain.folder.dto.response.FolderResponse$FolderDto(f.folderId, f.title) " +
            "FROM Folder f " +
            "ORDER BY f.createdAt")
    List<FolderResponse.FolderDto> findFolderDtoList();

    boolean existsByTitle(String title);
}
