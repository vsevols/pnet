package com.pnet.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PersistentDataService {
    public static <T> T loadObject(String path, Class<T> aClass, boolean ignoreUnknownProperties) throws IOException {
        return ReadJsonFile(aClass, path, ignoreUnknownProperties);
    }

    public static <T> void saveObject(String path, Object o) throws IOException {
        WriteJsonFile(path, o);
    }

    public static <T> T ReadJsonFile(Class<T> clazz, String path) throws IOException {
        return ReadJsonFile(clazz, path, false);
    }

        public static <T> T ReadJsonFile(Class<T> clazz, String path, boolean ignoreUnknownProperties) throws IOException {
        return fromJson(clazz, ReadFile(path), ignoreUnknownProperties);
    }

    private static String ReadFile(String path) throws IOException {
        FileReader fileReader = new FileReader(path);
        File file = new File(path);
        char[] result = new char[Math.toIntExact(file.length())];
        fileReader.read(result);
        return new String(result);
    }

    public static void WriteJsonFile(String path, Object object) throws IOException {
        WriteFile(path, toJson(object));
    }

    private static void WriteFile(String path, String buf) throws IOException {
        new File(new File(path).getParent()).mkdirs();
        FileWriter fileWriter = new FileWriter(path);
        fileWriter.write(buf);
        fileWriter.close();
    }

    public static boolean resourceExists(String path) {
        return new File(path).isFile();
    }

    public static <T> T fromJson(Class<T> clazz, String json) throws IOException {
        return fromJson(clazz, json, false);
    }

    public static <T> T fromJson(Class<T> clazz, String json, boolean ignoreUnknownProperties) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, !ignoreUnknownProperties);
        //https://stackoverflow.com/questions/45863678/json-parse-error-can-not-construct-instance-of-java-time-localdate-no-string-a
        mapper.registerModule(new JavaTimeModule());
        return mapper.readValue(json, clazz);
    }

    public static String toJson(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        //https://www.baeldung.com/jackson-inheritance
        objectMapper.enableDefaultTyping();
        objectMapper.registerModule(new JavaTimeModule());
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(obj);
    }
}
