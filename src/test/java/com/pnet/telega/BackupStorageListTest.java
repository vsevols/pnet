package com.pnet.telega;

import com.pnet.TestingUtils;
import it.tdlight.tdlib.TdApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.pnet.TestingUtils.getLocalTestsDataPath;
import static org.junit.jupiter.api.Assertions.*;

class BackupStorageListTest {

    private BackupStorageList<TdApi.Message> storage;

    @BeforeEach
    void setUp() {
        storage = new BackupStorageList<TdApi.Message>(getLocalTestsDataPath(getClass().getTypeName()));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void add_load_peek() {
        storage.add(new TdApi.Message());
        storage = new BackupStorageList<TdApi.Message>(storage.getPath());
        storage.load();
        assertNotEquals(null, storage.peek());
    }
}