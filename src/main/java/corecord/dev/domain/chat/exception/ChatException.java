package corecord.dev.domain.chat.exception;

import corecord.dev.domain.chat.status.ChatErrorStatus;
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
