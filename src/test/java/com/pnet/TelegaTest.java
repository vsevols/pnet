package com.pnet;

import com.pnet.abstractions.Message;
import com.pnet.secure.Config;
import com.pnet.telega.TdApiException;
import it.tdlight.tdlight.utils.CantLoadLibrary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.List;

import static java.lang.Thread.sleep;

public class TelegaTest {
    Telega telega;

    @Test
    public void TestSendMessageAndOnMessage() throws CantLoadLibrary, TdApiException {
        Debug.debug.dontInjectBackupedMessages=true;

        final String MESSAGE_TEXT =
                "TestSendMessageAndOnMessage() message text";

        final boolean[] isPassed = new boolean[1];

        telega.onMessage = new OnMessageHandler() {
            @Override
            public boolean onMessage(Message msg) {
                //TODO: request and check partyId (phone)
                //telega.getPartyId(msg.senderUserId)
                if(//msg.phone.value.equals(Config.ACCOUNT_PHONE)&&
                        msg.getText().equals(MESSAGE_TEXT))
                    isPassed[0] =true;
                return isPassed[0];
            }
        };
        telega.sendMessage(Config.ACCOUNT_PHONE, MESSAGE_TEXT);
        while (!isPassed[0])
            telega.process(1000);
    }

    @Test
    public void getUserLastSeen() throws Exception {
        Integer member = telega.getSupergroupMembers(Config.TEST_CHAT_NAME).get(0);
        telega.getUserLastSeen(member, Config.TEST_CHAT_NAME, Router.USER_CACHE_EXPIRED_MINUTES);
    }

    @BeforeEach
    public void setUp() throws CantLoadLibrary, IOException {
        Debug.debug=new Debug();
        telega = new Telega();
        telega.init();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void searchPublicChat() {
        Assertions.assertNotEquals(0, telega.searchPublicChat("SoVulgarChat"));
    }

    @Test
    void getSupergroupMembers() throws Exception {
        Assertions.assertNotEquals(0, telega.getSupergroupMembers("SoVulgarChat").size()>200);
    }

    @Test
    void getChatHistory() throws Exception {
        int id = telega.getMe();
        List<Message> chatHistory = telega.getChatHistory(id,0, 0, 10, true);
        Assertions.assertNotEquals(0, chatHistory.size());
    }

    @Test
    void getMeThenPrintMyId() throws TdApiException, Exception {
        System.out.println(telega.getMe());
    }
}