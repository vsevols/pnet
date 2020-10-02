package com.pnet;

import com.pnet.abstractions.User;
import com.pnet.routing.RoutingMessage;
import com.pnet.routing.VictimList;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class PublicationService {
    final Telega telega;
    final long observersChatId;

    void publish(RoutingMessage msg, VictimList victims) throws Exception {
        telega.sendMessage(observersChatId, getPublicationText(msg, victims));
    }

    private String getPublicationText(RoutingMessage msg, VictimList victims) {
        return String.format("%s (msgId: %d)\n%s", getVictimLabel(victims.getByKey(msg.getSenderUserId())),
                msg.getId(), msg.getText());
    }

    public void publishReproduced(RoutingMessage msg, VictimList victims) {
        ArrayList<Integer> reproducedTo = msg.reproducedTo;
        String labels=null;
        for (Integer userId :
                reproducedTo) {
            if(null==labels)
                labels=getVictimLabel(victims.getByKey(userId));
            else
            labels=String.format("%s, %s", labels, getVictimLabel(victims.getByKey(userId)));
        }

        try {
            telega.sendMessage(observersChatId, String.format("msgId: %d -> %s", msg.getId(), labels));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getVictimLabel(Victim victim) {
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
