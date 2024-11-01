package corecord.dev.domain.record.controller;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.web.UserId;
import corecord.dev.domain.record.constant.RecordSuccessStatus;
import corecord.dev.domain.record.dto.request.RecordRequest;
import corecord.dev.domain.record.dto.response.RecordResponse;
import corecord.dev.domain.record.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
public class RecordController {
    private final RecordService recordService;

    @PostMapping("/memo")
    public ResponseEntity<ApiResponse<RecordResponse.MemoRecordDto>> createMemoRecord(
            @UserId Long userId,
            @RequestBody RecordRequest.MemoRecordDto memoRecordDto
    ) {
        RecordResponse.MemoRecordDto recordResponse = recordService.createMemoRecord(userId, memoRecordDto);
        return ApiResponse.success(RecordSuccessStatus.MEMO_RECORD_CREATE_SUCCESS, recordResponse);
    }

    @GetMapping("/memo/{recordId}")
    public ResponseEntity<ApiResponse<RecordResponse.MemoRecordDto>> getMemoRecordDetail(
            @UserId Long userId,
            @PathVariable(name = "recordId") Long recordId
    ) {
        RecordResponse.MemoRecordDto recordResponse = recordService.getMemoRecordDetail(userId, recordId);
        return ApiResponse.success(RecordSuccessStatus.MEMO_RECORD_DETAIL_GET_SUCCESS, recordResponse);
    }

    @PostMapping("/memo/tmp")
    public ResponseEntity<ApiResponse<String>> saveTmpMemoRecord(
            @UserId Long userId,
            @RequestBody RecordRequest.TmpMemoRecordDto tmpMemoRecordDto
    ) {
        recordService.createTmpMemoRecord(userId, tmpMemoRecordDto);
        return ApiResponse.success(RecordSuccessStatus.MEMO_RECORD_TMP_CREATE_SUCCESS);
    }

    @GetMapping("/memo/tmp")
    public ResponseEntity<ApiResponse<RecordResponse.TmpMemoRecordDto>> getTmpMemoRecord(
            @UserId Long userId
    ) {
        RecordResponse.TmpMemoRecordDto recordResponse = recordService.getTmpMemoRecord(userId);
        return ApiResponse.success(RecordSuccessStatus.MEMO_RECORD_TMP_GET_SUCCESS, recordResponse);
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<RecordResponse.RecordListDto>> getRecordListByFolder(
        @UserId Long userId,
        @RequestParam(name = "folder", defaultValue = "all") String folder
    ) {
        RecordResponse.RecordListDto recordResponse = recordService.getRecordList(userId, folder);
        return ApiResponse.success(RecordSuccessStatus.RECORD_LIST_GET_SUCCESS, recordResponse);
    }

    @GetMapping("/keyword")
    public ResponseEntity<ApiResponse<RecordResponse.KeywordRecordListDto>> getRecordListByKeyword(
            @UserId Long userId,
            @RequestParam(name = "keyword") String keyword
    ) {
        RecordResponse.KeywordRecordListDto recordResponse = recordService.getKeywordRecordList(userId, keyword);
        return ApiResponse.success(RecordSuccessStatus.KEYWORD_RECORD_LIST_GET_SUCCESS, recordResponse);
    }

    @PatchMapping("/folder")
    public ResponseEntity<ApiResponse<String>> updateRecordForFolder(
            @UserId Long userId,
            @RequestBody RecordRequest.UpdateFolderDto updateFolderDto
    ) {
        recordService.updateFolder(userId, updateFolderDto);
        return ApiResponse.success(RecordSuccessStatus.RECORD_FOLDER_UPDATE_SUCCESS);
    }

}
