package corecord.dev.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceLoader {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static String getResourceContent(String resourcePath) {
        try {
            var resource = new ClassPathResource(resourcePath);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.NOT_FOUND);
        }
    }

    public static JsonNode getExampleRecordJson() {
        try {
            InputStream inputStream = new ClassPathResource("example-record.json").getInputStream();
            return objectMapper.readTree(inputStream);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
