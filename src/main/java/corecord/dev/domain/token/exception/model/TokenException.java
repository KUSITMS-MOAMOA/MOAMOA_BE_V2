package corecord.dev.domain.token.exception.model;

import corecord.dev.domain.token.exception.enums.TokenErrorStatus;
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
