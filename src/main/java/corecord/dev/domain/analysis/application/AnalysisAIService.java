package corecord.dev.domain.analysis.application;


import corecord.dev.domain.analysis.infra.openai.dto.response.AnalysisAiResponse;

public interface AnalysisAIService {
    AnalysisAiResponse generateAbilityAnalysis(String content);
}
