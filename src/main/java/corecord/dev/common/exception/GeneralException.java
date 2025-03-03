package corecord.dev.common.exception;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException{
    protected final BaseErrorStatus errorStatus;

    public GeneralException(BaseErrorStatus errorStatus) {
        super(errorStatus.getMessage());
        this.errorStatus = errorStatus;
    }
}
