package corecord.dev.domain.analysis.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Keyword {
    PROBLEM_SOLViNG_SKILL("문제해결능력"), ANALYTICAL_SKILL("분석력"),
    GLOBAL_SKILL("글로벌역량"), JUDGEMENT_SKILL("판단력"),

    TARGET_AWARENESS("목표의식"), MOMENTUM("추진력"),

    COMMUNICATION("커뮤니케이션"), LEADERSHIP("리더십"),
    COLLABORATION("협업능력"), ADAPTABILITY("적응력"),

    CREATIVITY("창의성"), LOGIC("논리성"),
    CHALLENGE_MINDSET("도전정신"), SELF_IMPROVEMENT("자기계발"), RESPONSIBILITY("책임감")
    ;

    private final String value;

    public String getValue() {
        return value;
    }

}
