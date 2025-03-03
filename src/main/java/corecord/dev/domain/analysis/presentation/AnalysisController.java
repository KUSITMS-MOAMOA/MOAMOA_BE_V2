package corecord.dev.domain.analysis.presentation;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.web.UserId;
import corecord.dev.domain.analysis.status.AnalysisSuccessStatus;
import corecord.dev.domain.analysis.domain.dto.request.AnalysisRequest;
import corecord.dev.domain.analysis.domain.dto.response.AnalysisResponse;
import corecord.dev.domain.analysis.application.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final AnalysisService analysisService;

    @PostMapping("/{recordId}")
    public ResponseEntity<ApiResponse<AnalysisResponse.AnalysisDto>> postAnalysis(
            @UserId Long userId,
            @PathVariable(name = "recordId") Long recordId
    ) {
        AnalysisResponse.AnalysisDto analysisResponse = analysisService.postAnalysis(userId, recordId);
        return ApiResponse.success(AnalysisSuccessStatus.ANALYSIS_POST_SUCCESS, analysisResponse);
    }

    @GetMapping("/{analysisId}")
    public ResponseEntity<ApiResponse<AnalysisResponse.AnalysisDto>> getAnalysis(
            @UserId Long userId,
            @PathVariable(name = "analysisId") Long analysisId
    ) {
        AnalysisResponse.AnalysisDto analysisResponse = analysisService.getAnalysis(userId, analysisId);
        return ApiResponse.success(AnalysisSuccessStatus.ANALYSIS_GET_SUCCESS, analysisResponse);
    }

    @PatchMapping("")
    public ResponseEntity<ApiResponse<AnalysisResponse.AnalysisDto>> updateAnalysis(
            @UserId Long userId,
            @RequestBody @Valid AnalysisRequest.AnalysisUpdateDto analysisUpdateDto
    ) {
        AnalysisResponse.AnalysisDto analysisResponse = analysisService.updateAnalysis(userId, analysisUpdateDto);
        return ApiResponse.success(AnalysisSuccessStatus.ANALYSIS_UPDATE_SUCCESS, analysisResponse);
    }

    @DeleteMapping("/{analysisId}")
    public ResponseEntity<ApiResponse<String>> deleteAnalysis(
            @UserId Long userId,
            @PathVariable(name = "analysisId") Long analysisId
    ) {
        analysisService.deleteAnalysis(userId, analysisId);
        return ApiResponse.success(AnalysisSuccessStatus.ANALYSIS_DELETE_SUCCESS);
    }
}
