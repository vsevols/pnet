package com.pnet;

import com.pnet.abstractions.User;
import com.pnet.routing.RoutingMessage;
import com.pnet.secure.Config;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PublicationService {
    final Telega telega;
    final long observersChatId;

    void publish(RoutingMessage msg) {
        telega.sendMessage(observersChatId, getPublicationText(msg));
    }

    private String getPublicationText(RoutingMessage msg) {
        String firstName = "Unknown", lastName = "Unknown";
        //TODO: Config.TEST_CHAT_NAME заменить на victim.groupName
        try {
            User user = telega.getUserInterface(msg.getSenderUserId(), Config.TEST_CHAT_NAME);
            firstName=user.getFirstName();
            lastName=user.getLastName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("%s %s (msgId: %d)\n%s", firstName, lastName, msg.getId(), msg.getText());
    }
}
