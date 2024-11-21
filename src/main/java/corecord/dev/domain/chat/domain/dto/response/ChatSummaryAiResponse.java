package corecord.dev.domain.chat.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ChatSummaryAiResponse {
    @JsonProperty("title")
    private String title;
    @JsonProperty("content")
    private String content;
}
