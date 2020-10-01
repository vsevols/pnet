package com.pnet.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class CountDown {
    public static final int NANOS_IN_MS = 1000000;
    private final LocalDateTime till;

    public CountDown(long milliseconds) {
        till= LocalDateTime.now().plusNanos(milliseconds* NANOS_IN_MS);
    }

    public boolean isExpired() {
        return till.isBefore(LocalDateTime.now());
    }

    public long getLeft() {
        return Duration.between(LocalDateTime.now(), till).toNanos() / NANOS_IN_MS;
    }

    public int getLeftInt() {
        return Math.toIntExact(getLeft());
    }
}
