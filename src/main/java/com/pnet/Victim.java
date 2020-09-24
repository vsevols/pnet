package com.pnet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

//https://stackoverflow.com/questions/39381474/cant-make-jackson-and-lombok-work-together
//@AllArgsConstructor
@RequiredArgsConstructor
public class Victim {
    public final int id;
    public final String groupName;
}
