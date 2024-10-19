package corecord.dev.domain.analysis.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Keyword {
    COMMUNICATION("커뮤니케이션"),
    TEAMWORK("팀워크"),
    LEADERSHIP("리더십")
    ;

    private final String value;

    public String getValue() {
        return value;
    }

}
