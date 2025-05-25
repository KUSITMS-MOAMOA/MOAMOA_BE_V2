package corecord.dev.domain.analysis.infra.gemini.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import corecord.dev.common.util.ResourceLoader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
public class GeminiAnalysisRequest {
    private static final String ABILITY_ANALYSIS_SYSTEM_CONTENT = ResourceLoader.getResourceContent("ability-analysis-prompt.txt");
    private static final String MEMO_SUMMARY_SYSTEM_CONTENT = ResourceLoader.getResourceContent("memo-summary-prompt.txt");

    private List<Content> contents;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Content{
        private List<Part> parts;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Part{
        private String text;
    }

    public GeminiAnalysisRequest(String content) {
        this.contents = List.of(new Content(List.of(new Part(content))));
    }

    public static GeminiAnalysisRequest createAbilityAnalysisRequest(String content) {
        return createRequest(ABILITY_ANALYSIS_SYSTEM_CONTENT, content);
    }

    public static GeminiAnalysisRequest createMemoSummaryRequest(String content) {
        return createRequest(MEMO_SUMMARY_SYSTEM_CONTENT, content);
    }

    private static GeminiAnalysisRequest createRequest(String systemContent, String content) {
        return new GeminiAnalysisRequest(systemContent + content);
    }
}
