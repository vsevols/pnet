package com.pnet;

import com.pnet.util.Keyed;
import lombok.RequiredArgsConstructor;

//https://stackoverflow.com/questions/39381474/cant-make-jackson-and-lombok-work-together
//@AllArgsConstructor
@RequiredArgsConstructor
public class Victim implements Keyed<Integer, Victim> {
    public final int id;
    public final String groupName;

    public Integer getKey() {
        return id;
    }
}
