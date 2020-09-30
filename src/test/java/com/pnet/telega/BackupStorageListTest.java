package com.pnet.telega;

import com.pnet.TestingUtils;
import it.tdlight.tdlib.TdApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static com.pnet.TestingUtils.getLocalTestsDataPath;
import static org.junit.jupiter.api.Assertions.*;

class BackupStorageListTest {

    private BackupStorageList<TdApi.Message> storage;

    @BeforeEach
    void setUp() throws IOException {
        String path = getLocalTestsDataPath(getClass().getTypeName());
        new File(path).delete();
        storage = new BackupStorageList<TdApi.Message>(path, true);
        storage.load();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void add_load_peek() throws IOException {
        storage.add(new TdApi.Message());
        storage = new BackupStorageList<TdApi.Message>(storage.getPath(), true);
        storage.load();
        assertNotEquals(null, storage.peek());
    }
}