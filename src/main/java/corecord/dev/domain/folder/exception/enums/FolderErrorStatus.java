package corecord.dev.domain.folder.exception.enums;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FolderErrorStatus implements BaseErrorStatus {
    DUPLICATED_FOLDER_TITLE(HttpStatus.BAD_REQUEST, "E0400_DUPLICATED_TITLE", "이미 존재하는 폴더 명입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
