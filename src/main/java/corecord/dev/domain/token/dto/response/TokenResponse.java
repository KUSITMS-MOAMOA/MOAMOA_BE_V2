package corecord.dev.domain.token.dto.response;

import lombok.Builder;
import lombok.Data;

public class TokenResponse {

    @Data
    @Builder
    public static class AccessTokenResponse {
        String accessToken;
    }
}
