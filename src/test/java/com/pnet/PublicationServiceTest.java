package com.pnet;

import com.pnet.routing.RoutingMessage;
import com.pnet.routing.VictimList;
import com.pnet.secure.Config;
import com.pnet.telega.MessageImpl;
import it.tdlight.tdlib.TdApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PublicationServiceTest extends AbstractPNetTest{

    private PublicationService publicationService;
    private Telega telega;

    @BeforeEach
    void setUp() {
        super.setUp();
        try {
            telega = new Telega(true);
            publicationService = new PublicationService(telega, new VictimService(telega),
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
        VictimList victims = new VictimList();
        victims.add(Config.TEST_VICTIM);
        publicationService.publish(routingMessage, victims);
        routingMessage.reproducedTo.add(victims.get(0).id);
        publicationService.publishReproduced(routingMessage, victims, 0);

        //TODO: Assert.doesNotContain(publishedMessage.getText(),"Unknown");
    }
}