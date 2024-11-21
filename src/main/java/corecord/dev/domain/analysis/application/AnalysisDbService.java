package corecord.dev.domain.analysis.application;

import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.analysis.domain.repository.AnalysisRepository;
import corecord.dev.domain.analysis.exception.AnalysisException;
import corecord.dev.domain.analysis.status.AnalysisErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AnalysisDbService {
    private final AnalysisRepository analysisRepository;

    @Transactional
    public void saveAnalysis(Analysis analysis) {
        analysisRepository.save(analysis);
    }

    @Transactional
    public void updateAnalysisContent(Analysis analysis, String content) {
        analysis.updateContent(content);
    }

    @Transactional
    public void updateAnalysisComment(Analysis analysis, String comment) {
        analysis.updateComment(comment);
    }

    @Transactional
    public void deleteAnalysis(Analysis analysis) {
        analysisRepository.delete(analysis);
    }

    @Transactional
    public void deleteAnalysisByUserId(Long userId) {
        analysisRepository.deleteAnalysisByUserId(userId);
    }

    public Analysis findAnalysisById(Long analysisId) {
        return analysisRepository.findAnalysisById(analysisId)
                .orElseThrow(() -> new AnalysisException(AnalysisErrorStatus.ANALYSIS_NOT_FOUND));
    }

}
