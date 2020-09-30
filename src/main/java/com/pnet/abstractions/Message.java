package com.pnet.abstractions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pnet.util.PNSystem;

import java.time.LocalDateTime;

public interface Message {
    long getId();

    boolean isOutgoing();

    int getSenderUserId();

    long getChatId();

    String getText();

    @JsonIgnore
    default LocalDateTime getLocalDateTime() {
        return PNSystem.unixTimeToLocalDateTime(getDate());
    }

    int getDate();
}
