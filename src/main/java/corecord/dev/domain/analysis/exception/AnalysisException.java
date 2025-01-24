package corecord.dev.domain.analysis.exception;

import corecord.dev.common.base.BaseErrorStatus;
import corecord.dev.common.exception.GeneralException;
import lombok.Getter;

@Getter
public class AnalysisException extends GeneralException {

    public AnalysisException(BaseErrorStatus errorStatus) {
        super(errorStatus);
    }

    @Override
    public String getMessage() {
        return errorStatus.getMessage();
    }
}
