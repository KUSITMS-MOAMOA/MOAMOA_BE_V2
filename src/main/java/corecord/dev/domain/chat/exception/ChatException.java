package corecord.dev.domain.chat.exception;

import corecord.dev.common.base.BaseErrorStatus;
import corecord.dev.common.exception.GeneralException;
import corecord.dev.domain.chat.status.ChatErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class ChatException extends GeneralException {

    public ChatException(BaseErrorStatus errorStatus) {
        super(errorStatus);
    }

    @Override
    public String getMessage() {
        return errorStatus.getMessage();
    }
}
