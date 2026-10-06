package com.blumo.tradecalculation.infrastructure.yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

final class YamlResources {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory())
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private YamlResources() {
    }

    static <T> T read(String classpathLocation, TypeReference<T> type) {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        try (InputStream inputStream = resource.getInputStream()) {
            return OBJECT_MAPPER.readValue(inputStream, type);
        } catch (IOException exception) {
            throw new IllegalStateException("failed to load " + classpathLocation, exception);
        }
    }
}
