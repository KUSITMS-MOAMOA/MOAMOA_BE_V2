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

    public ResponseCookie createTokenCookie(String tokenName, String token) {
        return ResponseCookie.from(tokenName, token)
                .httpOnly(true)
                .secure(true) // 배포 시 true로 설정
                .sameSite("None")
                .path("/")
                .maxAge(refreshTokenExpirationTime / 1000) // maxAge는 초 단위
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
