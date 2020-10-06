package com.pnet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

//https://stackoverflow.com/questions/39381474/cant-make-jackson-and-lombok-work-together
//@AllArgsConstructor
//@RequiredArgsConstructor
@Data
public class Victim {
    public final int id;
    public final String groupName;
    public boolean isRegularNotScam;
    public final String phone;
    public int tailOutgoingCount;
    public boolean isSendingFailedFlood;

    public static int getKey(Victim victim) {
        return victim.id;
    }
}
