package com.pnet;

import com.pnet.secure.Config;
import com.pnet.util.PersistentDataService;

import java.io.IOException;

public class TestingUtils {
    public static String getLocalTestsDataPath(String path) {
        return Config.toDataPath("tests/"+path);
    }

    public static <T> T loadObject(
            Class<T> tClass, String resourceName, boolean ignoreUnknownProperties) throws IOException {
        return PersistentDataService.loadObject(getLocalTestsDataPath(resourceName), tClass,
                ignoreUnknownProperties);
    }
}
