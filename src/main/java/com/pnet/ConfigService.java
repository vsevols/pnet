package com.pnet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.pnet.secure.Config;

import java.io.*;

import static com.pnet.PNSystem.promptString;

public class ConfigService {
    static Config loadConfig() throws IOException {
        return loadConfig(getDataFilePath());
    }

    static Config loadConfig(String path) throws IOException {
        if(!new File(path).isFile()) {
            if (!"initConfig".equals(promptString(
                    String.format("%s not exists. Type 'initConfig' to create new", getDataFilePath()))))
                throw new FileNotFoundException(getDataFilePath());
            else return null;
        }
        return new ConfigService().ReadJsonFile(Config.class, getDataFilePath());
    }

    static String getDataFilePath() {
        return Config.toDataPath("data.json");
    }

    static void saveConfig(Config config) {
        saveConfig(getDataFilePath(), config);
    }

    static void saveConfig(String path, Config config) {
        try {
            new ConfigService().WriteJsonFile(path, config);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

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
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        //https://www.baeldung.com/jackson-inheritance
        objectMapper.enableDefaultTyping();
        return ow.writeValueAsString(obj);
    }

}
