package corecord.dev.domain.analysis.exception.model;

import corecord.dev.domain.analysis.exception.enums.AnalysisErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnalysisException extends RuntimeException {
    private final AnalysisErrorStatus analysisErrorStatus;

    @Override
    public String getMessage() {
        return analysisErrorStatus.getMessage();
    }
}
