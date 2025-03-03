package corecord.dev.domain.analysis.application;

import corecord.dev.domain.analysis.domain.dto.request.AnalysisRequest;
import corecord.dev.domain.analysis.domain.dto.response.AnalysisResponse;
import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.record.domain.entity.Record;
import corecord.dev.domain.user.domain.entity.User;

public interface AnalysisService {

    Analysis createAnalysis(Record record, User user);
    AnalysisResponse.AnalysisDto postAnalysis(Long userId, Long recordId);
    AnalysisResponse.AnalysisDto getAnalysis(Long userId, Long analysisId);
    AnalysisResponse.AnalysisDto updateAnalysis(Long userId, AnalysisRequest.AnalysisUpdateDto analysisUpdateDto);
    void deleteAnalysis(Long userId, Long analysisId);

}
