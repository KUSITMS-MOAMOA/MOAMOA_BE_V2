package corecord.dev.domain.analysis.infra.gemini.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.common.util.GeminiUtil;
import corecord.dev.domain.analysis.application.AnalysisAIService;
import corecord.dev.domain.analysis.exception.AnalysisException;
import corecord.dev.domain.analysis.infra.clova.application.ClovaAnalysisAIService;
import corecord.dev.domain.analysis.infra.gemini.dto.request.GeminiAnalysisRequest;
import corecord.dev.domain.analysis.infra.openai.dto.response.AnalysisAiResponse;
import corecord.dev.domain.analysis.status.AnalysisErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClientException;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiAnalysisAIService implements AnalysisAIService {

    private final GeminiUtil geminiUtil;
    private final ClovaAnalysisAIService clovaAnalysisAIService;

    @Override
    public AnalysisAiResponse generateAbilityAnalysis(String content) {
        try {
            GeminiAnalysisRequest geminiRequest = GeminiAnalysisRequest.createAbilityAnalysisRequest(content);
            String responseBody = geminiUtil.postWebClient(geminiRequest);
            String aiResponse = geminiUtil.parseContentFromResponse(responseBody);
            log.info("airesponse = " + aiResponse);

            return parseAnalysisAiResponse(aiResponse);
        } catch (HttpServerErrorException | WebClientException e) {
            return clovaAnalysisAIService.generateAbilityAnalysis(content);
        }
    }

    @Override
    public String generateMemoSummary(String content) {
        try {
            GeminiAnalysisRequest geminiRequest = GeminiAnalysisRequest.createMemoSummaryRequest(content);
            String responseBody = geminiUtil.postWebClient(geminiRequest);
            return geminiUtil.parseContentFromResponse(responseBody);
        } catch (HttpServerErrorException | WebClientException e) {
            return clovaAnalysisAIService.generateMemoSummary(content);
        }
    }

    private AnalysisAiResponse parseAnalysisAiResponse(String aiResponse) {
        String cleanedAiResponse = aiResponse.replaceAll("```json\\s*", "").replaceAll("```", "");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(cleanedAiResponse, AnalysisAiResponse.class);
        } catch (JsonProcessingException e) {
            throw new AnalysisException(AnalysisErrorStatus.INVALID_ABILITY_ANALYSIS);
        }
    }
}
