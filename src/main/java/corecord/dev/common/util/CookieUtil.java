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
    public ResponseCookie createTokenCookie(String tokenName, String token, long expirationTime) {
        return ResponseCookie.from(tokenName, token)
                .domain("corecord.site")
                .httpOnly(true)
                .secure(true) // 배포 시 true로 설정
                .sameSite("None")
                .path("/")
                .maxAge(expirationTime / 1000) // maxAge는 초 단위
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

    public ResponseCookie deleteCookie(String cookieName) {
        return ResponseCookie.from(cookieName, "")
                .domain("corecord.site")
                .httpOnly(true)
                .secure(true) // 배포 시 true로 설정
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
    }
}
