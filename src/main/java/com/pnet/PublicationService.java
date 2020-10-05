package com.pnet;

import com.pnet.routing.RoutingMessage;
import com.pnet.routing.VictimList;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class PublicationService {
    final Telega telega;
    private final VictimService victimService;
    final long observersChatId;

    void publish(RoutingMessage msg, VictimList victims) throws Exception {
        telega.sendMessage(observersChatId, getPublicationText(msg, victims));
    }

    private String getPublicationText(RoutingMessage msg, VictimList victims) {
        return String.format("%s (msgId: %d)\n%s", victimService.getLabel(victims.getByKey(msg.getSenderUserId())),
                msg.getId(), msg.getText());
    }

    public void publishReproduced(RoutingMessage msg, VictimList victims, int incomingQueueSize) {
        ArrayList<Integer> reproducedTo = msg.reproducedTo;
        String labels=null;
        for (Integer userId :
                reproducedTo) {
            if(null==labels)
                labels= victimService.getLabel(victims.getByKey(userId));
            else
            labels=String.format("%s, %s", labels, victimService.getLabel(victims.getByKey(userId)));
        }

        try {
            telega.sendMessage(observersChatId,
                    String.format("msgId: %d -> %s\nincomingQueueSize: %d",
                            msg.getId(), labels, incomingQueueSize));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
