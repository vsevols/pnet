package com.pnet.abstractions;

import com.pnet.PNSystem;

import java.time.LocalDateTime;

public interface Message {
    long getId();

    boolean isOutgoing();

    int getSenderUserId();

    long getChatId();

    String getText();

    default LocalDateTime getLocalDateTime() {
        return PNSystem.unixTimeToLocalDateTime(getDate());
    }

    int getDate();
}
