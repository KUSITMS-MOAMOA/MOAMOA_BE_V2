package corecord.dev.domain.analysis.infra.gemini.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.common.util.GeminiUtil;
import corecord.dev.domain.analysis.application.AnalysisAIService;
import corecord.dev.domain.analysis.exception.AnalysisException;
import corecord.dev.domain.analysis.infra.gemini.dto.request.GeminiAnalysisRequest;
import corecord.dev.domain.analysis.infra.openai.dto.response.AnalysisAiResponse;
import corecord.dev.domain.analysis.status.AnalysisErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiAnalysisAIService implements AnalysisAIService {

    private final GeminiUtil geminiUtil;

    @Override
    public AnalysisAiResponse generateAbilityAnalysis(String content) {
        GeminiAnalysisRequest geminiRequest = GeminiAnalysisRequest.createAbilityAnalysisRequest(content);
        String responseBody = geminiUtil.postWebClient(geminiRequest);
        String aiResponse = geminiUtil.parseContentFromResponse(responseBody);

        return parseAnalysisAiResponse(aiResponse);
    }

    @Override
    public String generateMemoSummary(String content) {
        GeminiAnalysisRequest geminiRequest = GeminiAnalysisRequest.createMemoSummaryRequest(content);
        String responseBody = geminiUtil.postWebClient(geminiRequest);
        return geminiUtil.parseContentFromResponse(responseBody);
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
