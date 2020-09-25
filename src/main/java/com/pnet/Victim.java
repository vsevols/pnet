package com.pnet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

//https://stackoverflow.com/questions/39381474/cant-make-jackson-and-lombok-work-together
//@AllArgsConstructor
//@RequiredArgsConstructor
@Value
public class Victim {
    public final int id;
    public final String groupName;

    public static int getKey(Victim victim) {
        return victim.id;
    }
}
