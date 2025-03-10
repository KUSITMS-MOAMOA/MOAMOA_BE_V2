package corecord.dev.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class RedisLockUtil {

    private final StringRedisTemplate redisTemplate;

    public boolean acquireLock(String key, long timeout) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        return ops.setIfAbsent(key, "LOCKED", timeout, TimeUnit.SECONDS); // key가 없으면 true, 이미 있으면 false
    }

    public void releaseLock(String key) {
        redisTemplate.delete(key);
    }
}
