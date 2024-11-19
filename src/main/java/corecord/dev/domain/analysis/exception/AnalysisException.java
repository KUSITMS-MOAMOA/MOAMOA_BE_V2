package corecord.dev.domain.analysis.exception;

import corecord.dev.domain.analysis.status.AnalysisErrorStatus;
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
