package com.pnet;

import com.pnet.abstractions.Message;

public interface OnMessageHandler {
    void onMessage(Message msg);
}
