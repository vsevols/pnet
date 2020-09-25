package com.pnet.routing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pnet.Victim;
import com.pnet.util.KeyValueList;

import java.util.List;

@JsonIgnoreProperties({"empty"})
public class VictimList extends KeyValueList<Integer, Victim>{
    @Override
    protected Integer getKey(Victim victim) {
        return victim.id;
    }
}
