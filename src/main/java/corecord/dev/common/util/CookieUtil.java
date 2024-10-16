package corecord.dev.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtil {
    @Value("${jwt.refresh-token.expiration-time}")
    private long refreshTokenExpirationTime;

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .sameSite("None") // 배포 시 수정
                .secure(false) // 배포 시 수정
                .path("/")
                .maxAge(refreshTokenExpirationTime)
                .build();
    }

    public Cookie getCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh_token")) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public Cookie deleteRefreshTokenCookie() {
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        return cookie;
    }
}
