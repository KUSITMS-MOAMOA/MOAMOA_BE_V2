package corecord.dev.domain.token.controller;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.domain.token.constant.TokenSuccessStatus;
import corecord.dev.domain.token.dto.response.TokenResponse;
import corecord.dev.domain.token.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenController {
    private final TokenService tokenService;

    @GetMapping("/access-token")
    public ResponseEntity<ApiResponse<TokenResponse.AccessTokenResponse>> reissueAccessToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        TokenResponse.AccessTokenResponse accessTokenResponse = tokenService.reissueAccessToken(request, response);
        return ApiResponse.success(TokenSuccessStatus.REISSUE_ACCESS_TOKEN_SUCCESS, accessTokenResponse);
    }

    @GetMapping("/cookie/test")
    public ResponseEntity<ApiResponse<String>> test(
            HttpServletResponse response,
            @RequestHeader("registerToken") String registerToken
    ) {
        tokenService.test(response, registerToken);
        return ApiResponse.success(TokenSuccessStatus.SUCCESS_TEST);
    }

    @PostMapping("/cookie/test")
    public ResponseEntity<ApiResponse<String>> testPost(
            HttpServletResponse response,
            @RequestBody String registerToken
    ) {
        tokenService.test(response, registerToken);
        return ApiResponse.success(TokenSuccessStatus.SUCCESS_TEST);
    }

    @GetMapping("/cookie")
    public ResponseEntity<ApiResponse<String>> testGetCookie(
            @CookieValue(value = "tmpRefreshToken", required = false) String tmpRefreshToken
    ) {
        return ApiResponse.success(TokenSuccessStatus.SUCCESS_TEST, tmpRefreshToken);
    }
}
