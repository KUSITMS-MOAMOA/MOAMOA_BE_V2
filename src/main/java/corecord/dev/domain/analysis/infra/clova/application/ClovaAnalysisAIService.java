package corecord.dev.domain.analysis.infra.clova.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.common.util.ClovaUtil;
import corecord.dev.domain.analysis.application.AnalysisAIService;
import corecord.dev.domain.analysis.exception.AnalysisException;
import corecord.dev.domain.analysis.infra.clova.dto.request.ClovaAnalysisRequest;
import corecord.dev.domain.analysis.infra.openai.dto.response.AnalysisAiResponse;
import corecord.dev.domain.analysis.status.AnalysisErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClovaAnalysisAIService implements AnalysisAIService {

    private final ClovaUtil clovaUtil;

    @Override
    public AnalysisAiResponse generateAbilityAnalysis(String content) {
        ClovaAnalysisRequest clovaRequest = ClovaAnalysisRequest.createAbilityAnalysisRequest(content);
        String responseBody = clovaUtil.postWebClient(clovaRequest);
        String aiResponse = clovaUtil.parseContentFromResponse(responseBody);

        return parseAnalysisAiResponse(aiResponse);
    }

    @Override
    public String generateMemoSummary(String content) {
        ClovaAnalysisRequest clovaRequest = ClovaAnalysisRequest.createMemoSummaryRequest(content);
        String responseBody = clovaUtil.postWebClient(clovaRequest);
        return clovaUtil.parseContentFromResponse(responseBody);
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
