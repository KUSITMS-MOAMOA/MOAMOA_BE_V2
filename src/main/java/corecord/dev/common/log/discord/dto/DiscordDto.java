package corecord.dev.common.log.discord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class DiscordDto {
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class MessageDto {
        @JsonProperty("content")
        private String content;

        @JsonProperty("embeds")
        private List<EmbedDto> embeds;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class EmbedDto {
        @JsonProperty("title")
        private String title;

        @JsonProperty("description")
        private String description;
    }
}
