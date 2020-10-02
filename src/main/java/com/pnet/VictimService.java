package com.pnet;

import com.pnet.abstractions.User;
import lombok.Value;

@Value
public class VictimService {
    Telega telega;

    String getLabel(Victim victim) {
        try {
            User user = telega.tryObtainUser(victim.getId(), victim.groupName);
            if(null!=user)
                return String.format("%s %s", user.getFirstName(), user.getLastName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
}
