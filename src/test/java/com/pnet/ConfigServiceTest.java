package com.pnet;

import com.pnet.routing.RoutingMessage;
import com.pnet.secure.Config;
import com.pnet.telega.MessageImpl;
import it.tdlight.tdlib.TdApi;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.pnet.ConfigService.getDataFilePath;
import static org.junit.jupiter.api.Assertions.*;

class ConfigServiceTest {

    @Test
    void saveLoadConfig() throws IOException {
        Config config = new Config();
        config.incomingMessages.add(new RoutingMessage(new com.pnet.telega.MessageImpl(new TdApi.Message()), false));
        config.incomingMessages.add(new RoutingMessage(new com.pnet.routing.MessageImpl("testText"), false));
        ConfigService.saveConfig(Config.toDataPath("test.json"), config);
        config = ConfigService.loadConfig(Config.toDataPath("test.json"));
        assertTrue(config.incomingMessages.size()>=2);
        assertTrue("testText"==config.incomingMessages.get(1).getText());
    }
}