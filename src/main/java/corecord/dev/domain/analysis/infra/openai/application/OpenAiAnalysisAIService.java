package corecord.dev.domain.analysis.infra.openai.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.common.util.ResourceLoader;
import corecord.dev.domain.analysis.application.AnalysisAIService;
import corecord.dev.domain.analysis.infra.openai.dto.response.AnalysisAiResponse;
import corecord.dev.domain.analysis.status.AnalysisErrorStatus;
import corecord.dev.domain.analysis.exception.AnalysisException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
@RequiredArgsConstructor
public class OpenAiAnalysisAIService implements AnalysisAIService {
    private final OpenAiChatModel chatModel;
    private static final String ABILITY_ANALYSIS_SYSTEM_CONTENT = ResourceLoader.getResourceContent("ability-analysis-prompt.txt");
    private static final String SUMMARY_SYSTEM_CONTENT = ResourceLoader.getResourceContent("memo-summary-prompt.txt");

    @Override
    public AnalysisAiResponse generateAbilityAnalysis(String content) {
        String response = chatModel.call(ABILITY_ANALYSIS_SYSTEM_CONTENT + content);
        return parseAnalysisAiResponse(response);
    }

    @Override
    public String generateMemoSummary(String content) {
        return chatModel.call(SUMMARY_SYSTEM_CONTENT + content);
    }

    private AnalysisAiResponse parseAnalysisAiResponse(String aiResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(aiResponse, AnalysisAiResponse.class);
        } catch (JsonProcessingException e) {
            throw new AnalysisException(AnalysisErrorStatus.INVALID_ABILITY_ANALYSIS);
        }
    }
}
