package corecord.dev.common.util;

import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
import org.springframework.core.io.ClassPathResource;
import java.nio.charset.StandardCharsets;

public class ResourceLoader {

    public static String getResourceContent(String resourcePath) {
        try {
            var resource = new ClassPathResource(resourcePath);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.NOT_FOUND);
        }
    }
}
