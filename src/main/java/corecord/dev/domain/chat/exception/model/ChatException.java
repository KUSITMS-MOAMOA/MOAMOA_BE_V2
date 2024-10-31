package corecord.dev.domain.chat.exception.model;

import corecord.dev.domain.chat.exception.enums.ChatErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatException extends RuntimeException {
    private final ChatErrorStatus chatErrorStatus;

    @Override
    public String getMessage() {
        return chatErrorStatus.getMessage();
    }
}
