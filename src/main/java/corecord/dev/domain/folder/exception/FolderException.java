package corecord.dev.domain.folder.exception;

import corecord.dev.domain.folder.status.FolderErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FolderException extends RuntimeException {
    private final FolderErrorStatus folderErrorStatus;

    @Override
    public String getMessage() {
        return folderErrorStatus.getMessage();
    }
}
