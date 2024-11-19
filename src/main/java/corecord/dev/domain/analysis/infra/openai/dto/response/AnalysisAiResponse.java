package corecord.dev.domain.analysis.infra.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Setter
@Getter @Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisAiResponse {
    @JsonProperty("keywordList")
    private Map<String, String> keywordList;

    @JsonProperty("comment")
    private String comment;
}
