package corecord.dev.domain.user.entity;

import corecord.dev.domain.user.exception.enums.UserErrorStatus;
import corecord.dev.domain.user.exception.model.UserException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Status {
    UNIVERSITY_STUDENT("대학생"),
    GRADUATE_STUDENT("대학원생"),
    JOB_SEEKER("취준생"),
    INTERN("인턴"),
    EMPLOYED("취업준비생"),
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
