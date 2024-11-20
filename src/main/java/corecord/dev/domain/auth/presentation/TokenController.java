package corecord.dev.domain.auth.presentation;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.domain.auth.status.TokenSuccessStatus;
import corecord.dev.domain.auth.application.TokenService;
import corecord.dev.domain.user.domain.dto.response.UserResponse;
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

    @GetMapping("/reissue")
    public ResponseEntity<ApiResponse<Void>> reissueAccessToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        tokenService.reissueAccessToken(request, response);
        return ApiResponse.success(TokenSuccessStatus.REISSUE_ACCESS_TOKEN_SUCCESS);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse.UserDto>> issueToken(
            HttpServletResponse response,
            @RequestHeader("tmpToken") String tmpToken
    ) {
        UserResponse.UserDto issueTokenResponse = tokenService.issueTokens(response, tmpToken);
        return ApiResponse.success(TokenSuccessStatus.ISSUE_TOKENS_SUCCESS, issueTokenResponse);
    }
}
