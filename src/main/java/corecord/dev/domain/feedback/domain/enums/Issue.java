package corecord.dev.domain.feedback.domain.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Issue {
    // chat issue
    IRRELEVANT_ANSWER("제 경험과 관련없는 답변을 해요"),
    TOO_SIMPLE("질문이 너무 모호하거나 단순해요"),
    REPETITIVE("자연스럽지 않고 같은 질문을 반복해요"),
    NO_SPECIFIC("경험을 더 구체적으로 정리할 수 없어요"),

    // analysis issue
    IRRELEVANT_KEYWORD("경험과 관련없은 역량 키워드에요"),
    TOO_SHORT("코멘트가 짧고 피드백이 구체적이지 않아요"),
    AWKWARD("문장이 자연스럽지 않고 어색해요"),
    VAGUE_COMMENT("코멘트가 모호해서 개선할 점을 모르겠어요");

    private final String description;

    public String getDescription() {
        return description;
    }
}
