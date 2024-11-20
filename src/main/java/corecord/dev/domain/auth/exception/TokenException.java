package corecord.dev.domain.auth.exception;

import corecord.dev.domain.auth.status.TokenErrorStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TokenException extends RuntimeException {
    private final TokenErrorStatus tokenErrorStatus;

    @Override
    public String getMessage() {
        return tokenErrorStatus.getMessage();
    }
}
