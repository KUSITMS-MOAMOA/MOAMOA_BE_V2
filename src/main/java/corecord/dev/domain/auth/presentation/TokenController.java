package corecord.dev.domain.auth.presentation;

import corecord.dev.common.auth.TokenCookieManager;
import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.util.CookieUtil;
import corecord.dev.domain.auth.exception.TokenException;
import corecord.dev.domain.auth.status.TokenErrorStatus;
import corecord.dev.domain.auth.status.TokenSuccessStatus;
import corecord.dev.domain.auth.application.TokenService;
import corecord.dev.domain.user.domain.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenController {
    private final TokenService tokenService;
    private final TokenCookieManager tokenCookieManager;
    private final CookieUtil cookieUtil;

    @GetMapping("/reissue")
    public ResponseEntity<ApiResponse<Void>> reissueAccessToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = getRefreshTokenFromCookie(request);
        String accessToken = tokenService.reissueAccessToken(refreshToken);

        tokenCookieManager.removeAccessTokenCookie(response);
        tokenCookieManager.addAccessTokenCookie(response, accessToken);

        return ApiResponse.success(TokenSuccessStatus.REISSUE_ACCESS_TOKEN_SUCCESS);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse.UserDto>> issueToken(
            HttpServletResponse response,
            @RequestHeader("tmpToken") String tmpToken
    ) {
        UserResponse.UserDto issueTokenResponse = tokenService.issueTokens(tmpToken);

        tokenCookieManager.addAccessTokenCookie(response, issueTokenResponse.getAccessToken());
        tokenCookieManager.addRefreshTokenCookie(response, issueTokenResponse.getRefreshToken());

        return ApiResponse.success(TokenSuccessStatus.ISSUE_TOKENS_SUCCESS, issueTokenResponse);
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        String refreshToken = cookieUtil.getCookieValue(request, "refreshToken");
        if (refreshToken == null) {
            throw new TokenException(TokenErrorStatus.REFRESH_TOKEN_NOT_FOUND);
        }
        return refreshToken;
    }
}
