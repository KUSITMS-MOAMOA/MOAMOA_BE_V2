package corecord.dev.domain.auth.domain.enums;

import corecord.dev.domain.auth.status.TokenErrorStatus;

public enum TokenType {
    ACCESS("userId", TokenErrorStatus.INVALID_ACCESS_TOKEN),
    REFRESH("userId", TokenErrorStatus.INVALID_REFRESH_TOKEN),
    TMP("userId", TokenErrorStatus.INVALID_TMP_TOKEN);

    private final String claimKey;
    private final TokenErrorStatus errorStatus;

    TokenType(String claimKey, TokenErrorStatus errorStatus) {
        this.claimKey = claimKey;
        this.errorStatus = errorStatus;
    }

    public String getClaimKey() {
        return claimKey;
    }

    public TokenErrorStatus getErrorStatus() {
        return errorStatus;
    }
}


