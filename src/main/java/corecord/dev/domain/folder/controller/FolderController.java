package corecord.dev.domain.folder.controller;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.domain.folder.constant.FolderSuccessStatus;
import corecord.dev.domain.folder.dto.request.FolderRequest;
import corecord.dev.domain.folder.dto.response.FolderResponse;
import corecord.dev.domain.folder.service.FolderService;
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
            @RequestBody FolderRequest.FolderDto folderDto
    ) {
        FolderResponse.FolderDtoList folderResponse = folderService.createFolder(folderDto);

        return ApiResponse.success(FolderSuccessStatus.FOLDER_CREATE_SUCCESS, folderResponse);
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<ApiResponse<FolderResponse.FolderDtoList>> deleteFolder(
            @PathVariable(name = "folderId") Long folderId
    ) {
        FolderResponse.FolderDtoList folderResponse = folderService.deleteFolder(folderId);

        return ApiResponse.success(FolderSuccessStatus.FOLDER_DELETE_SUCCESS, folderResponse);
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<FolderResponse.FolderDtoList>> getFolders(
    ) {
        FolderResponse.FolderDtoList folderResponse = folderService.getFolderList();

        return ApiResponse.success(FolderSuccessStatus.FOLDER_GET_SUCCESS, folderResponse);
    }

    @PatchMapping("")
    public ResponseEntity<ApiResponse<FolderResponse.FolderDtoList>> updateFolder(
            @RequestBody FolderRequest.FolderUpdateDto folderDto
    ) {
        FolderResponse.FolderDtoList folderResponse = folderService.updateFolder(folderDto);

        return ApiResponse.success(FolderSuccessStatus.FOLDER_UPDATE_SUCCESS, folderResponse);
    }


}
