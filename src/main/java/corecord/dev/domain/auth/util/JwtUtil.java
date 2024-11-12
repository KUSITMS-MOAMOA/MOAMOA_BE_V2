package corecord.dev.domain.auth.util;

import corecord.dev.domain.auth.exception.enums.TokenErrorStatus;
import corecord.dev.domain.auth.exception.model.TokenException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.register-token.expiration-time}")
    private long REGISTER_TOKEN_EXPIRATION_TIME;

    @Value("${jwt.access-token.expiration-time}")
    private long ACCESS_TOKEN_EXPIRATION_TIME;

    @Value("${jwt.refresh-token.expiration-time}")
    private long REFRESH_TOKEN_EXPIRATION_TIME;

    // SecretKey 생성
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // JWT 토큰 생성 공통 로직
    private String createToken(String claimKey, String claimValue, long expirationTime) {
        return Jwts.builder()
                .claim(claimKey, claimValue)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    // 액세스 토큰 생성
    public String generateAccessToken(Long userId) {
        log.info("액세스 토큰이 발행되었습니다.");
        return createToken("userId", userId.toString(), ACCESS_TOKEN_EXPIRATION_TIME);
    }

    // 리프레쉬 토큰 생성
    public String generateRefreshToken(Long userId) {
        log.info("리프레쉬 토큰이 발행되었습니다.");
        return createToken("userId", userId.toString(), REFRESH_TOKEN_EXPIRATION_TIME);
    }

    // 레지스터 토큰 생성
    public String generateRegisterToken(String providerId) {
        log.info("레지스터 토큰이 발행되었습니다.");
        return createToken("providerId", providerId, REGISTER_TOKEN_EXPIRATION_TIME);
    }

    // 임시 토큰 생성
    public String generateTmpToken(Long userId) {
        log.info("임시 토큰이 발행되었습니다.");
        return createToken("userId", userId.toString(), 3600000);
    }

    // 토큰 검증 공통 로직
    private boolean isTokenValid(String token, String claimKey, TokenErrorStatus errorStatus) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(this.getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            if (claims.getPayload().getExpiration().before(new Date())) {
                log.warn("{}이 만료되었습니다.", errorStatus.getMessage());
                throw new TokenException(errorStatus);
            }

            String claimValue = claims.getPayload().get(claimKey, String.class);
            if (claimValue == null || claimValue.isEmpty()) {
                log.warn("토큰에 {} 클레임이 없습니다.", claimKey);
                throw new TokenException(errorStatus);
            }

            return true;

        } catch (ExpiredJwtException e) {
            log.warn("토큰이 만료되었습니다: {}", e.getMessage());
            throw new TokenException(errorStatus);
        } catch (JwtException | MalformedJwtException e) {
            log.warn("유효하지 않은 토큰입니다: {}", e.getMessage());
            throw new TokenException(errorStatus);
        }
    }

    // 레지스터 토큰 유효성 검증
    public boolean isRegisterTokenValid(String token) {
        return isTokenValid(token, "providerId", TokenErrorStatus.INVALID_REGISTER_TOKEN);
    }

    // 액세스 토큰 유효성 검증
    public boolean isAccessTokenValid(String token) {
        return isTokenValid(token, "userId", TokenErrorStatus.INVALID_ACCESS_TOKEN);
    }

    // 리프레쉬 토큰 유효성 검증
    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token, "userId", TokenErrorStatus.INVALID_REFRESH_TOKEN);
    }

    // 임시 토큰 유효성 검증
    public boolean isTmpTokenValid(String token) {
        return isTokenValid(token, "userId", TokenErrorStatus.INVALID_TMP_TOKEN);
    }

    // 토큰에서 클레임 추출 공통 로직
    private String getClaimFromToken(String token, String claimKey, TokenErrorStatus errorStatus) {
        try {
            return Jwts.parser()
                    .verifyWith(this.getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get(claimKey, String.class);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 토큰입니다.");
            throw new TokenException(errorStatus);
        }
    }

    // 레지스터 토큰에서 providerId 추출
    public String getProviderIdFromToken(String token) {
        return getClaimFromToken(token, "providerId", TokenErrorStatus.INVALID_REGISTER_TOKEN);
    }

    // 액세스 토큰에서 userId 추출
    public String getUserIdFromAccessToken(String token) {
        return getClaimFromToken(token, "userId", TokenErrorStatus.INVALID_ACCESS_TOKEN);
    }

    // 리프레쉬 토큰에서 userId 추출
    public String getUserIdFromRefreshToken(String token) {
        return getClaimFromToken(token, "userId", TokenErrorStatus.INVALID_REFRESH_TOKEN);
    }

    // 임시 토큰에서 userId 추출
    public String getUserIdFromTmpToken(String token) {
        return getClaimFromToken(token, "userId", TokenErrorStatus.INVALID_TMP_TOKEN);
    }
}
