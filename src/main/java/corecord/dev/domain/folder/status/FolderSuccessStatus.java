package corecord.dev.domain.folder.status;

import corecord.dev.common.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FolderSuccessStatus implements BaseSuccessStatus {

    FOLDER_CREATE_SUCCESS(HttpStatus.CREATED, "S601", "폴더 생성이 성공적으로 완료되었습니다."),
    FOLDER_DELETE_SUCCESS(HttpStatus.OK, "S605", "폴더 삭제가 성공적으로 완료되었습니다."),
    FOLDER_GET_SUCCESS(HttpStatus.OK, "S603", "폴더 리스트 조회가 성공적으로 완료되었습니다."),
    FOLDER_UPDATE_SUCCESS(HttpStatus.OK, "S604", "폴더명 수정이 성공적으로 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
