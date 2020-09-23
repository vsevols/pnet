package com.pnet.abstractions;

public interface Message {
    long getId();

    boolean isOutgoing();

    int getSenderUserId();

    long getChatId();

    String getText();
}
