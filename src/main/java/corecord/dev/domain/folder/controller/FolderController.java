package corecord.dev.domain.folder.controller;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.domain.folder.constant.FolderSuccessStatus;
import corecord.dev.domain.folder.dto.request.FolderRequest;
import corecord.dev.domain.folder.dto.response.FolderResponse;
import corecord.dev.domain.folder.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
