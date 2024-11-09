package corecord.dev.domain.auth.exception.model;

import corecord.dev.domain.auth.exception.enums.TokenErrorStatus;
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
