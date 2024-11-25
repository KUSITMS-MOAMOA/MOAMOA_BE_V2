package corecord.dev.domain.auth.presentation;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenController {
    private final TokenService tokenService;
    private final CookieUtil cookieUtil;

    @Value("${jwt.access-token.expiration-time}")
    private long accessTokenExpirationTime;

    @Value("${jwt.refresh-token.expiration-time}")
    private long refreshTokenExpirationTime;

    @GetMapping("/reissue")
    public ResponseEntity<ApiResponse<Void>> reissueAccessToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = getRefreshTokenFromCookie(request);
        String accessToken = tokenService.reissueAccessToken(refreshToken);

        // 기존 AccessToken 삭제
        ResponseCookie accessTokenCookie = cookieUtil.deleteCookie("accessToken");
        response.addHeader("Set-Cookie", accessTokenCookie.toString());

        // 새 AccessToken 발급
        ResponseCookie newAccessTokenCookie = cookieUtil.createTokenCookie("accessToken", accessToken, accessTokenExpirationTime);
        response.addHeader("Set-Cookie", newAccessTokenCookie.toString());
        return ApiResponse.success(TokenSuccessStatus.REISSUE_ACCESS_TOKEN_SUCCESS);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse.UserDto>> issueToken(
            HttpServletResponse response,
            @RequestHeader("tmpToken") String tmpToken
    ) {
        UserResponse.UserDto issueTokenResponse = tokenService.issueTokens(tmpToken);

        // 새 AccessToken 발급
        ResponseCookie accessTokenCookie = cookieUtil.createTokenCookie("accessToken", issueTokenResponse.getAccessToken(), accessTokenExpirationTime);
        response.addHeader("Set-Cookie", accessTokenCookie.toString());

        // 새 RefreshToken 발급
        ResponseCookie refreshTokenCookie = cookieUtil.createTokenCookie("refreshToken", issueTokenResponse.getRefreshToken(), refreshTokenExpirationTime);
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

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
