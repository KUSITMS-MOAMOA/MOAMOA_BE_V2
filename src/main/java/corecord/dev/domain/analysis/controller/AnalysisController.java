package corecord.dev.domain.analysis.controller;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.web.UserId;
import corecord.dev.domain.analysis.constant.AnalysisSuccessStatus;
import corecord.dev.domain.analysis.dto.request.AnalysisRequest;
import corecord.dev.domain.analysis.dto.response.AnalysisResponse;
import corecord.dev.domain.analysis.service.AnalysisService;
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

    @GetMapping("/keyword")
    public ResponseEntity<ApiResponse<AnalysisResponse.KeywordListDto>> getKeywordList(
            @UserId Long userId
    ) {
        AnalysisResponse.KeywordListDto analysisResponse = analysisService.getKeywordList(userId);
        return ApiResponse.success(AnalysisSuccessStatus.KEYWORD_LIST_GET_SUCCESS, analysisResponse);
    }

    @GetMapping("/graph")
    public ResponseEntity<ApiResponse<AnalysisResponse.GraphDto>> getKeywordGraph(
            @UserId Long userId
    ) {
        AnalysisResponse.GraphDto analysisResponse = analysisService.getKeywordGraph(userId);
        return ApiResponse.success(AnalysisSuccessStatus.KEYWORD_GRAPH_GET_SUCCESS, analysisResponse);
    }
}
