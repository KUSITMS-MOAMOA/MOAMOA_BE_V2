package corecord.dev.domain.record.controller;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.web.UserId;
import corecord.dev.domain.record.constant.RecordSuccessStatus;
import corecord.dev.domain.record.dto.request.RecordRequest;
import corecord.dev.domain.record.dto.response.RecordResponse;
import corecord.dev.domain.record.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
