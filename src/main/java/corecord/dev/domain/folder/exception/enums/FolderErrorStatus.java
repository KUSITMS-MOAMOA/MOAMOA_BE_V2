package corecord.dev.domain.folder.exception.enums;

import corecord.dev.common.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FolderErrorStatus implements BaseErrorStatus {
    DUPLICATED_FOLDER_TITLE(HttpStatus.BAD_REQUEST, "E0400_DUPLICATED_TITLE", "이미 존재하는 폴더 명입니다."),
    OVERFLOW_FOLDER_TITLE(HttpStatus.BAD_REQUEST, "E0400_OVERFLOW_TITLE", "폴더 명은 15자 이내여야 합니다."),
    USER_FOLDER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E401_FOLDER_UNAUTHORIZED", "유저가 폴더에 대한 권한이 없습니다."),
    FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND, "E0404_FOLDER", "존재하지 않는 폴더입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
