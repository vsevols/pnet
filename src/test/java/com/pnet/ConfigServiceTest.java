package com.pnet;

import com.pnet.routing.RoutingMessage;
import com.pnet.secure.Config;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ConfigServiceTest {

    @Test
    void saveLoadConfig() throws IOException {
        Config config = Config.emptyConfig();
        config.victims.put(123, new Victim(123, "", ""), false);
        config.incomingMessages.add(new RoutingMessage(
                new com.pnet.routing.MessageImpl("testText"), false));
        ConfigService.saveConfig(Config.toDataPath("test.json"), config);
        config = ConfigService.loadConfig(Config.toDataPath("test.json"));
        assertEquals(1, config.victims.size());
        assertEquals(123, config.victims.getByKey(123).getId());
        assertEquals(1, config.incomingMessages.size());
        assertEquals("testText", config.incomingMessages.get(0).getText());
    }

    @Disabled
    @Test
    void loadConfigTdApiMessage() throws IOException {
        Config config;
        String path = Config.toDataPath("loadConfigTdApiMessage.json");
        config = ConfigService.loadConfig(path);
        assertEquals(1, config.incomingMessages.size());
        assertEquals("testText", config.incomingMessages.get(0).getText());
    }
}