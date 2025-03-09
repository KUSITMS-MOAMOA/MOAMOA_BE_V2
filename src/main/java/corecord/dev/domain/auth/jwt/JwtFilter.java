package corecord.dev.domain.auth.jwt;

import corecord.dev.common.util.CookieUtil;
import corecord.dev.domain.auth.exception.TokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String accessToken = cookieUtil.getCookieValue(request, "accessToken");
            if (accessToken != null && jwtUtil.isAccessTokenValid(accessToken)) {
                String userId = jwtUtil.getUserIdFromAccessToken(accessToken);
                Authentication authToken = new UsernamePasswordAuthenticationToken(
                        userId, // principal로 userId 사용
                        null,  // credentials는 필요 없으므로 null
                        null   // authorities는 비워둠 (필요한 경우 권한 추가)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (TokenException e) {
            logger.error("JWT Filter Error", e);
            handleTokenException(response, e);
            return;
        } catch (Exception e) {
            logger.error("JWT Filter Error", e);
            handleException(response, e);
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // 특정 경로는 필터링하지 않도록 설정
        String path = request.getRequestURI();
        return path.startsWith("/oauth2/authorization/kakao") || path.startsWith("/api/users/register") || path.startsWith("/api/token") || path.startsWith("/actuator/health") || path.startsWith("/login/oauth2/code/**");
    }

    private void handleTokenException(HttpServletResponse response, TokenException e) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = String.format("{\"isSuccess\": \"false\", \"code\": \"%s\", \"message\": \"%s\"}", e.getErrorStatus().getCode(), e.getMessage());
        response.getWriter().write(jsonResponse);
    }

    private void handleException(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = String.format("{\"isSuccess\": \"false\", \"code\": \"E9999\", \"message\": \"%s\"}", e.getMessage());
        response.getWriter().write(jsonResponse);
    }

}
