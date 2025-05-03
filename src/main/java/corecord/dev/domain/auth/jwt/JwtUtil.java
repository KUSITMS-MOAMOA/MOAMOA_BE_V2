package corecord.dev.domain.auth.jwt;

import corecord.dev.domain.auth.domain.enums.TokenType;
import corecord.dev.domain.auth.exception.TokenException;
import corecord.dev.domain.auth.status.TokenErrorStatus;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

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
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

    private String createToken(Map<String, String> claims, long expirationTime) {
        var builder = Jwts.builder()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime));

        claims.forEach(builder::claim);
        return builder.signWith(getSigningKey()).compact();
    }

    // === 토큰 생성 ===

    public String generateAccessToken(Long userId) {
        log.info("Access token generated.");
        return createToken(Map.of("userId", userId.toString()), ACCESS_TOKEN_EXPIRATION_TIME);
    }

    public String generateRefreshToken(Long userId) {
        log.info("Refresh token generated.");
        return createToken(Map.of("userId", userId.toString()), REFRESH_TOKEN_EXPIRATION_TIME);
    }

    public String generateRegisterToken(String providerId, String provider) {
        log.info("Register token generated.");
        return createToken(Map.of("providerId", providerId, "provider", provider), REGISTER_TOKEN_EXPIRATION_TIME);
    }

    public String generateTmpToken(Long userId) {
        log.info("Temporary token generated.");
        return createToken(Map.of("userId", userId.toString()), REGISTER_TOKEN_EXPIRATION_TIME);
    }

    // === 토큰 검증 ===

    public boolean isTokenValid(String token, TokenType type) {
        return validateClaims(token, type.getErrorStatus(), type.getClaimKey());
    }

    public boolean isRegisterTokenValid(String token) {
        return validateClaims(token, TokenErrorStatus.INVALID_REGISTER_TOKEN, "providerId", "provider");
    }

    private boolean validateClaims(String token, TokenErrorStatus errorStatus, String... requiredKeys) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            for (String key : requiredKeys) {
                String value = claims.get(key, String.class);
                if (value == null || value.isEmpty()) {
                    throw new TokenException(errorStatus);
                }
            }

            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Expired token: {}", e.getMessage());
            throw new TokenException(errorStatus);
        } catch (SignatureException | MalformedJwtException | IllegalArgumentException e) {
            log.warn("Invalid token: {}", e.getMessage());
            throw new TokenException(errorStatus);
        }
    }

    // === 클레임 추출 ===

    public String getClaimFromToken(String token, TokenType type) {
        return getClaimFromToken(token, type.getClaimKey(), type.getErrorStatus());
    }

    private String getClaimFromToken(String token, String claimKey, TokenErrorStatus errorStatus) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get(claimKey, String.class);
        } catch (Exception e) {
            throw new TokenException(errorStatus);
        }
    }

    public String getProviderIdFromToken(String token) {
        return getClaimFromToken(token, "providerId", TokenErrorStatus.INVALID_REGISTER_TOKEN);
    }

    public String getProviderFromToken(String token) {
        return getClaimFromToken(token, "provider", TokenErrorStatus.INVALID_REGISTER_TOKEN);
    }

    public String getUserIdFromAccessToken(String token) {
        return getClaimFromToken(token, TokenType.ACCESS);
    }

    public String getUserIdFromRefreshToken(String token) {
        return getClaimFromToken(token, TokenType.REFRESH);
    }

    public String getUserIdFromTmpToken(String token) {
        return getClaimFromToken(token, TokenType.TMP);
    }
}
