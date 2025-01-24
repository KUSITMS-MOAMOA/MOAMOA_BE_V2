package corecord.dev.domain.ability.exception;

import corecord.dev.common.base.BaseErrorStatus;
import corecord.dev.common.exception.GeneralException;
import lombok.Getter;

@Getter
public class AbilityException extends GeneralException {

    public AbilityException(BaseErrorStatus errorStatus) {
        super(errorStatus);
    }

    @Override
    public String getMessage() {
        return errorStatus.getMessage();
    }
}
