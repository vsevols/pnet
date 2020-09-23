package com.pnet.abstractions;

import com.pnet.telega.MessageImpl;

public interface Chat {
    long getId();

    Message getLastMessage();
}
