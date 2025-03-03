package corecord.dev.domain.record.exception;

import corecord.dev.common.base.BaseErrorStatus;
import corecord.dev.common.exception.GeneralException;
import lombok.Getter;

@Getter
public class RecordException extends GeneralException {

    public RecordException(BaseErrorStatus errorStatus) {
        super(errorStatus);
    }

    @Override
    public String getMessage() {
        return errorStatus.getMessage();
    }
}
