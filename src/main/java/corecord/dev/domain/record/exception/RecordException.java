package corecord.dev.domain.record.exception;

import corecord.dev.domain.record.status.RecordErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecordException extends RuntimeException {
    private final RecordErrorStatus recordErrorStatus;

    @Override
    public String getMessage() {
        return recordErrorStatus.getMessage();
    }
}
