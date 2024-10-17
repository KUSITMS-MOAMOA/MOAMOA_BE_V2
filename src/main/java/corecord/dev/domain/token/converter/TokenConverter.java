package corecord.dev.domain.token.converter;

import corecord.dev.domain.token.dto.response.TokenResponse;

public class TokenConverter {
    public static TokenResponse.AccessTokenResponse toAccessTokenResponse(String newAccessToken) {
        return TokenResponse.AccessTokenResponse.builder().accessToken(newAccessToken).build();
    }
}
