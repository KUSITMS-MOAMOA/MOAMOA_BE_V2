package corecord.dev.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Data
public class ChatSummaryAiResponse {
    @JsonProperty("title")
    private String title;
    @JsonProperty("content")
    private String content;
}
