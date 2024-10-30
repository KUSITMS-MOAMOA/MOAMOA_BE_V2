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
//            @UserId Long userId,
            @RequestBody RecordRequest.MemoRecordDto memoRecordDto
    ) {
        RecordResponse.MemoRecordDto recordResponse = recordService.createMemoRecord(1L, memoRecordDto);
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
}
