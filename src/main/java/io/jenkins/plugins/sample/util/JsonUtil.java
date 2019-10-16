package io.jenkins.plugins.sample.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintStream;

public class JsonUtil {
    private static ObjectMapper mapper = new ObjectMapper();



    public static String asString(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T asObject(String content, Class<T> valueType) {
        if (valueType == String.class)
            return (T) content;
        try {
            return mapper.readValue(content, valueType);
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonNode asJsonNode(String content) {
        try {
            return mapper.readTree(content);
        } catch (Exception e) {
            return null;
        }
    }
}
