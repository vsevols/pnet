package com.pnet.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class PNSystem {
    public static String promptString(String prompt) {
        System.out.print(prompt+System.lineSeparator());
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String str = "";
        try {
            str = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static LocalDateTime unixTimeToLocalDateTime(int unixTime) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(
                unixTime),
                TimeZone.getDefault().toZoneId()
        );
    }

    public static void UnsupportedOperation(Object object) {
        throw new UnsupportedOperationException(String.valueOf(object));
    }
}
