package corecord.dev.domain.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "tmpToken", timeToLive = 3600000)
@AllArgsConstructor
@Builder
public class TmpToken {
    @Id
    private String tmpToken;
    private Long userId;

    @Builder
    public static TmpToken of(String tmpToken, Long userId) {
        return TmpToken.builder()
                .tmpToken(tmpToken)
                .userId(userId)
                .build();
    }
}
