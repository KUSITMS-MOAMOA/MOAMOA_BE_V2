package corecord.dev.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.status.ErrorStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.error("LOGIN FAILED: {}", exception.getMessage());

        // 응답 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        // 에러 응답 생성
        ApiResponse<Object> errorResponse = ApiResponse.error(ErrorStatus.UNAUTHORIZED).getBody();

        // 응답에 에러 메시지 작성
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
