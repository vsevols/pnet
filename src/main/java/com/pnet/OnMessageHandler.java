package com.pnet;

import com.pnet.telega.MessageImpl;

public interface OnMessageHandler {
    void onMessage(MessageImpl msg);
}
