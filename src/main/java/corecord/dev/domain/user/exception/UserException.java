package corecord.dev.domain.user.exception;

import corecord.dev.domain.user.status.UserErrorStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserException extends RuntimeException {
    private final UserErrorStatus userErrorStatus;

    @Override
    public String getMessage() {
        return userErrorStatus.getMessage();
    }
}
