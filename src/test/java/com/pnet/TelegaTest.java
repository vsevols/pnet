package com.pnet;

import com.pnet.abstractions.Message;
import com.pnet.secure.Config;
import com.pnet.telega.MessageImpl;
import com.pnet.telega.TdApiException;
import it.tdlight.tdlib.TdApi;
import it.tdlight.tdlight.utils.CantLoadLibrary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.*;

import java.util.List;

public class TelegaTest {
    Telega telega;

    @Test
    public void TestSendMessageAndOnMessage() throws CantLoadLibrary, TdApiException {
        final String MESSAGE_TEXT =
                "TestSendMessageAndOnMessage() message text";

        final boolean[] isPassed = new boolean[1];

        telega.onMessage = new OnMessageHandler() {
            @Override
            public void onMessage(Message msg) {
                //TODO: request and check partyId (phone)
                //telega.getPartyId(msg.senderUserId)
                if(//msg.phone.value.equals(Config.ACCOUNT_PHONE)&&
                        msg.getText().equals(MESSAGE_TEXT))
                    isPassed[0] =true;
            }
        };
        telega.sendMessage(Config.ACCOUNT_PHONE, MESSAGE_TEXT);
        while (!isPassed[0])
            telega.process(1000);
    }

    @Test
    public void getUserLastSeen() throws Exception {
        Integer member = telega.getSupergroupMembers(Config.TEST_CHAT_NAME).get(0);
        telega.getUserLastSeen(member, Config.TEST_CHAT_NAME);
    }

    @BeforeEach
    public void setUp() throws CantLoadLibrary {
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
    void getChatHistory() throws TdApiException {
        int id = telega.userIdByPhone(Config.TEST_PHONE);
        List<Message> chatHistory = telega.getChatHistory(id,0, 0, 10);
        Assertions.assertNotEquals(0, chatHistory.size());
    }

    @Test
    void getMeThenPrintMyId() throws TdApiException {
        System.out.println(telega.getMe());
    }
}