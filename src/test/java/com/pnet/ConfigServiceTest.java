package com.pnet;

import com.pnet.routing.RoutingMessage;
import com.pnet.secure.Config;
import com.pnet.telega.MessageImpl;
import it.tdlight.tdlib.TdApi;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static com.pnet.ConfigService.getDataFilePath;
import static org.junit.jupiter.api.Assertions.*;

class ConfigServiceTest {

    @Test
    void saveLoadConfig() throws IOException {
        Config config = emptyConfig();
        config.victims.put(123, new Victim(123, ""));
        config.incomingMessages.add(new RoutingMessage(
                new com.pnet.routing.MessageImpl(0, true, 0, 0,"testText"), false));
        ConfigService.saveConfig(Config.toDataPath("test.json"), config);
        config = ConfigService.loadConfig(Config.toDataPath("test.json"));
        assertEquals(1, config.victims.size());
        assertEquals(123, config.victims.get(123).id);
        assertEquals(1, config.incomingMessages.size());
        assertEquals("testText", config.incomingMessages.get(0).getText());
    }

    private Config emptyConfig() {
        return new Config(new ConcurrentHashMap<>(), new ArrayList<>(), new ArrayList<>());
    }

    @Disabled
    @Test
    void loadConfigTdApiMessage() throws IOException {
        Config config = emptyConfig();
        String path = Config.toDataPath("loadConfigTdApiMessage.json");
        config = ConfigService.loadConfig(path);
        assertEquals(1, config.incomingMessages.size());
        assertEquals("testText", config.incomingMessages.get(0).getText());
    }
}