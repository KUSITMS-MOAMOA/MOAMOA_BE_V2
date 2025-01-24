package corecord.dev.domain.auth.exception;

import corecord.dev.common.base.BaseErrorStatus;
import corecord.dev.common.exception.GeneralException;
import lombok.Getter;

@Getter
public class TokenException extends GeneralException {

    public TokenException(BaseErrorStatus errorStatus) {
        super(errorStatus);
    }

    @Override
    public String getMessage() {
        return errorStatus.getMessage();
    }
}
