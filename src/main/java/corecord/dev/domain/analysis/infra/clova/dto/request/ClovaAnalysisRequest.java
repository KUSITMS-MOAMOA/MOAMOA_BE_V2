package corecord.dev.domain.analysis.infra.clova.dto.request;

import corecord.dev.common.util.ResourceLoader;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class ClovaAnalysisRequest {
    private static final int MAX_TOKENS = 500;
    private static final String ABILITY_ANALYSIS_SYSTEM_CONTENT = ResourceLoader.getResourceContent("ability-analysis-prompt.txt");
    private static final String MEMO_SUMMARY_SYSTEM_CONTENT = ResourceLoader.getResourceContent("memo-summary-prompt.txt");

    private List<Map<String, String>> messages;
    private final double topP = 0.8;
    private final int topK = 0;
    private final int maxTokens;
    private final double temperature = 0.5;
    private final double repeatPenalty = 5.0;
    private final boolean includeAiFilters = true;
    private final int seed = 0;

    public ClovaAnalysisRequest(List<Map<String, String>> messages, int max_tokens) {
        this.messages = messages;
        this.maxTokens = max_tokens;
    }

    public static ClovaAnalysisRequest createAbilityAnalysisRequest(String content) {
        return createRequest(ABILITY_ANALYSIS_SYSTEM_CONTENT, content);
    }

    public static ClovaAnalysisRequest createMemoSummaryRequest(String content) {
        return createRequest(MEMO_SUMMARY_SYSTEM_CONTENT, content);
    }

    public static ClovaAnalysisRequest createRequest(String systemContent, String userContent) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemContent));
        messages.add(Map.of("role", "user", "content", userContent));
        return new ClovaAnalysisRequest(messages, MAX_TOKENS);
    }
}
