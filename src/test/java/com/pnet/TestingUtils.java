package com.pnet;

import com.pnet.secure.Config;
import com.pnet.util.PersistentDataService;

import java.io.File;
import java.io.IOException;

public class TestingUtils {
    public static String getLocalTestsDataPath(String path) {
        return"d:\\pnetData\\tests\\"+path;
    }

    public static <T> T loadObject(
            Class<T> tClass, String resourceName, boolean ignoreUnknownProperties) throws IOException {
        return PersistentDataService.loadObject(getLocalTestsDataPath(resourceName), tClass,
                ignoreUnknownProperties);
    }

    public static void saveObject(Object o, String resourceName) throws IOException {
        PersistentDataService.saveObject(getLocalTestsDataPath(resourceName), o);
    }

    public static void saveObjectTemp(Object o) throws IOException {
        File temp = new File(getLocalTestsDataPath("temp"));
        if(temp.isFile())
            temp.delete();
        saveObject(o, temp.getName());
    }

    public static <T> T loadObjectTemp(Class<T> tClass, boolean ignoreUnknownProperties) throws IOException {
        return loadObject(tClass, "temp", ignoreUnknownProperties);
    }
}
