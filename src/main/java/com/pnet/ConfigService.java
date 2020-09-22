package com.pnet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigService {
    private void WriteFile(String path, String buf) throws IOException {
        new File(new File(path).getParent()).mkdirs();
        FileWriter fileWriter = new FileWriter(path);
        fileWriter.write(buf);
        fileWriter.close();
    }

    private String ReadFile(String path) throws IOException {
        FileReader fileReader = new FileReader(path);
        File file = new File(path);
        char[] result = new char[Math.toIntExact(file.length())];
        fileReader.read(result);
        return new String(result);
    }

    public <T> T ReadJsonFile(Class<T> clazz, String path) throws IOException {
        return fromJson(clazz, ReadFile(path));
    }

    public void WriteJsonFile(String path, Object object) throws IOException {
        WriteFile(path, toJson(object));
    }

    public <T> T fromJson(Class<T> clazz, String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, clazz);
    }

    public String toJson(Object obj) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(obj);
    }

}
