package com.pnet;

import com.pnet.routing.RoutingMessage;
import com.pnet.secure.Config;
import com.pnet.telega.MessageImpl;
import it.tdlight.tdlib.TdApi;
import it.tdlight.tdlight.utils.CantLoadLibrary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class PublicationServiceTest extends AbstractPNetTest{

    private PublicationService publicationService;
    private Telega telega;

    @BeforeEach
    void setUp() {
        super.setUp();
        try {
            telega = new Telega(true);
            publicationService = new PublicationService(telega,
                    telega.checkChatInviteLink(Config.TEST_OUTBOUND_CHAT_INVITELINK));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @AfterEach
    void tearDown() {
        //telega.process(10000);
    }

    @Test
    void publish() throws Exception {
        TdApi.Message msg = TestingUtils.loadObject(TdApi.Message.class,
                "com.pnet.PublicationServiceTest.publish", true);

        RoutingMessage routingMessage = RoutingMessage.fromMessage(new MessageImpl(msg));
        publicationService.publish(routingMessage);
    }
}