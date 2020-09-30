package com.pnet;

import com.pnet.abstractions.Message;

public interface OnMessageHandler {
    boolean onMessage(Message msg);
}
