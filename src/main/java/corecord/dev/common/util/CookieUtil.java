package corecord.dev.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieUtil {
    @Value("${jwt.refresh-token.expiration-time}")
    private long refreshTokenExpirationTime;

    @Value("${jwt.register-token.expiration-time}")
    private long registerTokenExpirationTime;

    public ResponseCookie createTokenCookie(String tokenName, String token) {
        return ResponseCookie.from(tokenName, token)
                .httpOnly(true)
                .secure(false) // 배포 시 수정
                .path("/")
                .build();
    }

    public String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public Cookie deleteCookie(String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        return cookie;
    }
}
