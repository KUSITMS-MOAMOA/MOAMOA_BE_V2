package corecord.dev.domain.analysis.infra.clova.application;

import corecord.dev.domain.analysis.application.AnalysisAIService;
import corecord.dev.domain.analysis.infra.openai.dto.response.AnalysisAiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClovaAnalysisAIService implements AnalysisAIService {
    @Override
    public AnalysisAiResponse generateAbilityAnalysis(String content) {
        return null;
    }

    @Override
    public String generateMemoSummary(String content) {
        return "";
    }
}
