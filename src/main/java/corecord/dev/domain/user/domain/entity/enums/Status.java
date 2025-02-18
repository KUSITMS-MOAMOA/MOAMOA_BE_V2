package corecord.dev.domain.user.domain.entity.enums;

import corecord.dev.domain.user.status.UserErrorStatus;
import corecord.dev.domain.user.exception.UserException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Status {
    UNIVERSITY_STUDENT("대학생"),
    GRADUATE_STUDENT("대학원생"),
    JOB_SEEKER("취업 준비생"),
    INTERN("인턴"),
    EMPLOYED("재직 중"),
    OTHER("기타");

    private final String value;

    public String getValue() {
        return value;
    }

    public static Status getStatus(String value) {
        for (Status status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new UserException(UserErrorStatus.INVALID_USER_STATUS);
    }
}
