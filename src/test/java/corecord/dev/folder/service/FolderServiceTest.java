package corecord.dev.folder.service;

import corecord.dev.domain.folder.domain.dto.request.FolderRequest;
import corecord.dev.domain.folder.domain.dto.response.FolderResponse;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.folder.exception.FolderException;
import corecord.dev.domain.folder.domain.repository.FolderRepository;
import corecord.dev.domain.folder.application.FolderService;
import corecord.dev.domain.user.domain.entity.Status;
import corecord.dev.domain.user.domain.entity.User;
import corecord.dev.domain.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FolderServiceTest {
    @Mock
    private FolderRepository folderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FolderService folderService;

    private final Long testId = 1L;
    private final String testTitle = "Test folder";


    @Test
    @DisplayName("새로운 폴더 생성 기능")
    void createFolder() {
        // Given
        User user = createMockUser(testId, "Test User");
        Folder folder = createMockFolder(testId, testTitle, user);
        user.getFolders().add(folder);

        when(userRepository.findById(testId)).thenReturn(Optional.of(user));
        when(folderRepository.save(any(Folder.class))).thenReturn(folder);
        when(folderRepository.findFolderDtoList(user)).thenReturn(List.of(
                FolderResponse.FolderDto.builder()
                        .folderId(testId)
                        .title(testTitle)
                        .build()
        ));

        // When
        FolderRequest.FolderDto request = FolderRequest.FolderDto.builder()
                .title(testTitle)
                .build();

        FolderResponse.FolderDtoList response = folderService.createFolder(testId, request);

        // Then
        verify(userRepository).findById(testId);
        verify(folderRepository).save(any(Folder.class));
        verify(folderRepository).findFolderDtoList(user);

        assertThat(response.getFolderDtoList()).isNotNull();
        assertThat(response.getFolderDtoList().get(0).getTitle()).isEqualTo(testTitle);
        assertThat(response.getFolderDtoList().get(0).getFolderId()).isEqualTo(testId);
    }

    @Test
    @DisplayName("폴더명 수정 기능")
    void updateFolder() {
        // Given
        String updatedTitle = "Updated Title";

        User user = createMockUser(testId, "Test User");
        Folder folder = createMockFolder(testId, testTitle, user);
        user.getFolders().add(folder);

        when(userRepository.findById(testId)).thenReturn(Optional.of(user));
        when(folderRepository.findById(testId)).thenReturn(Optional.ofNullable(folder));
        when(folderRepository.existsByTitle(updatedTitle)).thenReturn(false);
        when(folderRepository.findFolderDtoList(user)).thenReturn(List.of(
                FolderResponse.FolderDto.builder()
                        .folderId(testId)
                        .title(updatedTitle)
                        .build()
        ));

        // When
        FolderRequest.FolderUpdateDto request = FolderRequest.FolderUpdateDto.builder()
                .folderId(testId)
                .title(updatedTitle)
                .build();

        FolderResponse.FolderDtoList response = folderService.updateFolder(testId, request);

        // Then
        verify(folderRepository).findById(testId);
        verify(folderRepository).existsByTitle(updatedTitle);
        verify(folderRepository).findFolderDtoList(user);

        assertThat(response.getFolderDtoList()).isNotNull();
        assertThat(response.getFolderDtoList().get(0).getTitle()).isEqualTo(updatedTitle);
        assertThat(response.getFolderDtoList().get(0).getFolderId()).isEqualTo(testId);
        assertThat(folder.getTitle()).isEqualTo(updatedTitle);
    }

    @Test
    @DisplayName("중복된 폴더 생성 시 오류 반환 테스트")
    void createDuplicateFolder() {
        // Given
        User user = createMockUser(testId, "Test User");
        Folder folder1 = createMockFolder(testId, testTitle, user);
        user.getFolders().add(folder1);

        Folder folder2 = createMockFolder(testId + 1, testTitle, user);

        when(userRepository.findById(testId)).thenReturn(Optional.of(user));
        when(folderRepository.existsByTitle(testTitle)).thenReturn(true);

        // When & Then
        FolderRequest.FolderDto request = FolderRequest.FolderDto.builder()
                .title(testTitle)
                .build();

        assertThat(user.getFolders()).isEqualTo(List.of(folder1));
        assertThrows(FolderException.class, () -> folderService.createFolder(testId, request));

        verify(userRepository).findById(testId);
        verify(folderRepository).existsByTitle(testTitle);
        verify(folderRepository, never()).save(folder2);
    }

    private User createMockUser(Long userId, String nickName) {
        return User.builder()
                .userId(userId)
                .providerId("Test Provider")
                .nickName(nickName)
                .status(Status.GRADUATE_STUDENT)
                .folders(new ArrayList<>())
                .build();
    }

    private Folder createMockFolder(Long folderId, String title, User user) {
        return Folder.builder()
                .folderId(folderId)
                .title(title)
                .user(user)
                .build();
    }

}
