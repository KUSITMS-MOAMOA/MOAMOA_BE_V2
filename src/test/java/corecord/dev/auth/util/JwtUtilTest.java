package corecord.dev.auth.util;

import corecord.dev.domain.auth.jwt.JwtUtil;
import corecord.dev.domain.auth.status.TokenErrorStatus;
import corecord.dev.domain.auth.exception.TokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String SECRET_KEY = "testsecretkeytestsecretkeytestsecretkeytestsecretkeytestsecretkeytestsecretkeytestsecretkeytestsecretkey";
    private final long REGISTER_TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1 hour
    private final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24; // 24 hours
    private final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 7 days
    private SecretKey key;
    private Long userId;
    private String providerId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        providerId = "testProvider";
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtil, "REGISTER_TOKEN_EXPIRATION_TIME", REGISTER_TOKEN_EXPIRE_TIME);
        ReflectionTestUtils.setField(jwtUtil, "ACCESS_TOKEN_EXPIRATION_TIME", ACCESS_TOKEN_EXPIRE_TIME);
        ReflectionTestUtils.setField(jwtUtil, "REFRESH_TOKEN_EXPIRATION_TIME", REFRESH_TOKEN_EXPIRE_TIME);
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

    @Test
    @DisplayName("액세스 토큰 생성 및 유효성 검사")
    void generateAndValidateAccessToken() {
        // when
        String accessToken = jwtUtil.generateAccessToken(userId);

        // then
        assertThat(accessToken).isNotNull().isNotEmpty();
        assertThat(accessToken.split("\\.")).hasSize(3);

        Claims payload = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

        assertThat(payload.get("userId", String.class)).isEqualTo(userId.toString());
    }

    @Test
    @DisplayName("리프레쉬 토큰 생성 및 유효성 검사")
    void generateAndValidateRefreshToken() {
        // when
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        // then
        assertThat(refreshToken).isNotNull().isNotEmpty();
        assertThat(refreshToken.split("\\.")).hasSize(3);

        Claims payload = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        assertThat(payload.get("userId", String.class)).isEqualTo(userId.toString());
    }

    @Test
    @DisplayName("레지스터 토큰 생성 및 유효성 검사")
    void generateAndValidateRegisterToken() {
        // when
        String registerToken = jwtUtil.generateRegisterToken(providerId);

        // then
        assertThat(registerToken).isNotNull().isNotEmpty();
        assertThat(registerToken.split("\\.")).hasSize(3);

        Claims payload = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(registerToken)
                .getBody();

        assertThat(payload.get("providerId", String.class)).isEqualTo(providerId);
    }

    @Test
    @DisplayName("임시 토큰 생성 및 유효성 검사")
    void generateAndValidateTmpToken() {
        // when
        String tmpToken = jwtUtil.generateTmpToken(userId);

        // then
        assertThat(tmpToken).isNotNull().isNotEmpty();
        assertThat(tmpToken.split("\\.")).hasSize(3);

        Claims payload = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(tmpToken)
                .getBody();

        assertThat(payload.get("userId", String.class)).isEqualTo(userId.toString());
    }

    @Test
    @DisplayName("만료된 액세스 토큰 예외 발생")
    void expiredAccessTokenThrowsException() {
        // given
        String expiredAccessToken = Jwts.builder()
                .setSubject(userId.toString())
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // 이미 만료된 시간 설정
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();

        // then
        TokenException exception = assertThrows(TokenException.class, () -> jwtUtil.isAccessTokenValid(expiredAccessToken));
        assertThat(exception.getErrorStatus()).isEqualTo(TokenErrorStatus.INVALID_ACCESS_TOKEN);
    }


    @Test
    @DisplayName("유효하지 않은 토큰 예외 발생")
    void invalidTokenThrowsException() {
        // given
        String invalidToken = "invalid.token";

        // then
        TokenException exception = assertThrows(TokenException.class, () -> jwtUtil.isAccessTokenValid(invalidToken));
        assertThat(exception.getErrorStatus()).isEqualTo(TokenErrorStatus.INVALID_ACCESS_TOKEN);
    }
}
