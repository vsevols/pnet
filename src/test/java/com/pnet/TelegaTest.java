package com.pnet;

import com.pnet.abstractions.Message;
import com.pnet.routing.MessageImpl;
import com.pnet.routing.RoutingMessage;
import com.pnet.secure.Config;
import com.pnet.telega.TdApiException;
import com.pnet.util.PersistentDataService;
import it.tdlight.tdlight.utils.CantLoadLibrary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.List;

import static com.pnet.TestingUtils.getLocalTestsDataPath;
import static java.lang.Thread.sleep;

public class TelegaTest {
    Telega telega;

    @Test
    public void TestSendMessageAndOnMessage() throws CantLoadLibrary, TdApiException {
        Debug.debug.dontInjectBackupedMessages=true;

        final String MESSAGE_TEXT =
                "TestSendMessageAndOnMessage() message text";

        sendMessageRoundtrip(MESSAGE_TEXT);
    }

    @Test
    public void givenCyrillicText_WhenSendMessageToSelfReceiveSerializeDeserialize_ThenTextEquals() throws TdApiException, IOException {
        final String text = "Тест кириллицы";
        RoutingMessage msg = RoutingMessage.fromMessage(sendMessageRoundtrip(text));

        TestingUtils.saveObjectTemp(msg);
        msg = TestingUtils.loadObjectTemp(RoutingMessage.class, true);
        Assertions.assertEquals(text, msg.getText());
    }

    private Message sendMessageRoundtrip(String text) throws TdApiException {
        final boolean[] isPassed = new boolean[1];
        final Message[] result = new Message[1];

        telega.onMessage = new OnMessageHandler() {
            @Override
            public boolean onMessage(Message msg) {
                //telega.getPartyId(msg.senderUserId)
                if(//msg.phone.value.equals(Config.ACCOUNT_PHONE)&&
                        msg.getText().equals(text))
                    result[0] =msg;

                return null!=result[0];
            }
        };
        telega.sendMessage(Config.ACCOUNT_PHONE, text);
        while (null==result[0])
            telega.process(1000);

        return result[0];
    }

    @Test
    public void getUserLastSeen() throws Exception {
        Integer member = telega.getSupergroupMembers(Config.TEST_CHAT_NAME).get(0);
        telega.getUserLastSeen(member, Config.TEST_CHAT_NAME, Router.USER_CACHE_EXPIRED_MINUTES);
    }

    @BeforeEach
    public void setUp() throws CantLoadLibrary, IOException {
        Debug.debug=new Debug(true);
        Debug.debug.dontInjectBackupedMessages=true;
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
    void getMe() throws Exception {
        Assertions.assertEquals(Config.ME_ID, telega.getMe());
    }

    @Test
    void givenGetMe_WhenGetChatHistory_ThenHistorySizeNotEquals0() throws Exception {
        int id = telega.getMe();
        List<Message> chatHistory = telega.getChatHistory(id,0, 0, 10, true);
        Assertions.assertNotEquals(0, chatHistory.size());
    }
    @Test
    void givenTEST_OUTBOUND_USER_ID_WhenGetChatHistory_ThenHistorySizeNotEquals0() throws Exception {
        List<Message> chatHistory = telega.getChatHistory(Config.TEST_OUTBOUND_USER_ID, 0, 0, 10, false);
        Assertions.assertNotEquals(0, chatHistory.size());
    }

    @Test
    void getMeThenPrintMyId() throws TdApiException, Exception {
        System.out.println(telega.getMe());
    }

    @Disabled
    @Test
    void forwardMessage() throws Exception, TdApiException {
        Debug.debug.dontInjectBackupedMessages=true;
        MessageImpl message= PersistentDataService.loadObject(
                getLocalTestsDataPath(getClass().getTypeName()+"#forwardMessage"),
                MessageImpl.class, true);
        //telega.getUserInterface(message.getSenderUserId(), Config.TEST_CHAT_NAME);
        //telega.getChatHistory(message.getSenderUserId(), message.getId(), 0, 1, true);
        //telega.forwardMessage(message, Config.TEST_OUTBOUND_CHAT_ID);
        telega.forwardMessage(message,
                telega.checkChatInviteLink(Config.TEST_OUTBOUND_CHAT_INVITELINK));
    }

    @Test
    void checkChatInviteLink() throws Exception {
        Assertions.assertNotEquals(0, telega.checkChatInviteLink(Config.TEST_OUTBOUND_CHAT_INVITELINK));
    }

}