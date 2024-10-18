package corecord.dev.domain.folder.exception.enums;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class FolderErrorStatus implements BaseErrorStatus {

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
