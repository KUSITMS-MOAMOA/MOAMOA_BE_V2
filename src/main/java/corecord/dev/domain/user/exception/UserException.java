package corecord.dev.domain.user.exception;

import corecord.dev.common.base.BaseErrorStatus;
import corecord.dev.common.exception.GeneralException;
import lombok.Getter;

@Getter
public class UserException extends GeneralException {

    public UserException(BaseErrorStatus errorStatus) {
        super(errorStatus);
    }

    @Override
    public String getMessage() {
        return errorStatus.getMessage();
    }
}
