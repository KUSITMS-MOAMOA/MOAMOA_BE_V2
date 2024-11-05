package corecord.dev.domain.analysis.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter @Data
public class AnalysisAiResponse {
    @JsonProperty("keywordList")
    private Map<String, String> keywordList;

    @JsonProperty("comment")
    private String comment;
}
