package com.pnet.routing;

import com.pnet.Victim;
import com.pnet.util.KeyValueList;

public class VictimList extends KeyValueList<Integer, Victim> {
    @Override
    protected Integer getKey(Victim victim) {
        return victim.id;
    }
}
