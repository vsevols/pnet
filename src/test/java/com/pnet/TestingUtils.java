package com.pnet;

import com.pnet.secure.Config;

public class TestingUtils {
    public static String getLocalTestsDataPath(String path) {
        return Config.toDataPath("tests/"+path);
    }
}
