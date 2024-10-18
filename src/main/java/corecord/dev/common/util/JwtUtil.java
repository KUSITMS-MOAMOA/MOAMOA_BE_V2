package corecord.dev.common.util;

import corecord.dev.domain.token.exception.enums.TokenErrorStatus;
import corecord.dev.domain.token.exception.model.TokenException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
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

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Long userId) {
        log.info("액세스 토큰이 발행되었습니다.");
        return Jwts.builder()
                .claim("userId", userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(this.getSigningKey())
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        log.info("리프레쉬 토큰이 발행되었습니다.");
        return Jwts.builder()
                .claim("userId", userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(this.getSigningKey())
                .compact();
    }


    public String generateRegisterToken(String providerId) {
        log.info("레지스터 토큰이 발행되었습니다.");
        return Jwts.builder()
                .claim("providerId", providerId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REGISTER_TOKEN_EXPIRATION_TIME))
                .signWith(this.getSigningKey())
                .compact();
    }

    public boolean isRegisterTokenValid(String token) {
        return isTokenValid(token, "providerId", TokenErrorStatus.INVALID_REGISTER_TOKEN);
    }

    public boolean isAccessTokenValid(String token) {
        return isTokenValid(token, "userId", TokenErrorStatus.INVALID_ACCESS_TOKEN);
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token, "userId", TokenErrorStatus.INVALID_REFRESH_TOKEN);
    }

    private boolean isTokenValid(String token, String claimKey, TokenErrorStatus invalidTokenErrorStatus) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(this.getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            Date expirationDate = claims.getPayload().getExpiration();
            if (expirationDate.before(new Date())) {
                log.warn("{}이 만료되었습니다.", invalidTokenErrorStatus.getMessage());
                throw new TokenException(invalidTokenErrorStatus);
            }

            String claimValue = claims.getPayload().get(claimKey, String.class);
            if (claimValue == null || claimValue.isEmpty()) {
                log.warn("토큰에 {} 클레임이 없습니다.", claimKey);
                throw new TokenException(invalidTokenErrorStatus);
            }

            return true;

        } catch (ExpiredJwtException e) {
            log.warn("토큰이 만료되었습니다: {}", e.getMessage());
            throw new TokenException(invalidTokenErrorStatus);
        } catch (JwtException e) {
            log.warn("유효하지 않은 토큰입니다: {}", e.getMessage());
            throw new TokenException(invalidTokenErrorStatus);
        }
    }

    public String getProviderIdFromToken(String token) {
        try {
            log.info("토큰 파싱");
            log.info("token: {}", token);
            return Jwts.parser()
                    .verifyWith(this.getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("providerId", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 토큰입니다.");
            throw new TokenException(TokenErrorStatus.INVALID_REGISTER_TOKEN);
        }
    }

    public String getUserIdFromAccessToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(this.getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("userId", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 토큰입니다.");
            throw new TokenException(TokenErrorStatus.INVALID_ACCESS_TOKEN);
        }
    }

    public String getUserIdFromRefreshToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(this.getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("userId", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 토큰입니다.");
            throw new TokenException(TokenErrorStatus.INVALID_REFRESH_TOKEN);
        }
    }
}
