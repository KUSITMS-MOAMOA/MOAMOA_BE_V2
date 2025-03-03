package corecord.dev.domain.chat.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatSummaryAiResponse {
    @JsonProperty("title")
    private String title;
    @JsonProperty("content")
    private String content;
}
