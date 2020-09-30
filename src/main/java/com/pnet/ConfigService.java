package com.pnet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pnet.secure.Config;
import com.pnet.util.PersistentDataService;

import java.io.*;

import static com.pnet.util.PNSystem.promptString;

public class ConfigService {

    public static Config loadConfig() throws IOException {
        return loadConfig(getDataFilePath());
    }

    static Config loadConfig(String path) throws IOException {
        if(!new File(path).isFile()) {
            if (!"initConfig".equals(promptString(
                    String.format("%s not exists. Type 'initConfig' to create new", path))))
                throw new FileNotFoundException(path);
            else{
                Config config = Config.emptyConfig();
                saveConfig(config);
                return config;
            }
        }
        return PersistentDataService.ReadJsonFile(Config.class, path);
    }

    public static String getDataFilePath() {
        return Config.toDataPath("data.json");
    }

    public static void saveConfig(Config config) {
        saveConfig(getDataFilePath(), config);
    }

    static void saveConfig(String path, Config config) {
        try {
            PersistentDataService.WriteJsonFile(path, config);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

}
