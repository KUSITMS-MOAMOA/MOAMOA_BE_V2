package corecord.dev.common.exception;

import corecord.dev.common.status.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException{
    private final ErrorStatus errorStatus;
}
