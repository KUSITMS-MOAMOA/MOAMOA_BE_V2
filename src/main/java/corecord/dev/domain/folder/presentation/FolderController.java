package corecord.dev.domain.folder.presentation;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.web.UserId;
import corecord.dev.domain.folder.status.FolderSuccessStatus;
import corecord.dev.domain.folder.domain.dto.request.FolderRequest;
import corecord.dev.domain.folder.domain.dto.response.FolderResponse;
import corecord.dev.domain.folder.application.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/folders")
public class FolderController {
    private final FolderService folderService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<FolderResponse.FolderDtoList>> createFolder(
            @UserId Long userId,
            @RequestBody @Valid FolderRequest.FolderDto folderDto
    ) {
        FolderResponse.FolderDtoList folderResponse = folderService.createFolder(userId, folderDto);
        return ApiResponse.success(FolderSuccessStatus.FOLDER_CREATE_SUCCESS, folderResponse);
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<ApiResponse<FolderResponse.FolderDtoList>> deleteFolder(
            @UserId Long userId,
            @PathVariable(name = "folderId") Long folderId
    ) {
        FolderResponse.FolderDtoList folderResponse = folderService.deleteFolder(userId, folderId);
        return ApiResponse.success(FolderSuccessStatus.FOLDER_DELETE_SUCCESS, folderResponse);
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<FolderResponse.FolderDtoList>> getFolders(
            @UserId Long userId
    ) {
        FolderResponse.FolderDtoList folderResponse = folderService.getFolderList(userId);
        return ApiResponse.success(FolderSuccessStatus.FOLDER_GET_SUCCESS, folderResponse);
    }

    @PatchMapping("")
    public ResponseEntity<ApiResponse<FolderResponse.FolderDtoList>> updateFolder(
            @UserId Long userId,
            @RequestBody @Valid FolderRequest.FolderUpdateDto folderDto
    ) {
        FolderResponse.FolderDtoList folderResponse = folderService.updateFolder(userId, folderDto);
        return ApiResponse.success(FolderSuccessStatus.FOLDER_UPDATE_SUCCESS, folderResponse);
    }


}
