package com.pnet;

import com.pnet.abstractions.Message;
import com.pnet.abstractions.User;
import com.pnet.routing.MessageImpl;
import com.pnet.routing.RoutingMessage;
import com.pnet.secure.Config;
import com.pnet.telega.TdApiException;
import com.pnet.util.PersistentDataService;
import it.tdlight.tdlib.TdApi;
import it.tdlight.tdlight.utils.CantLoadLibrary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.util.List;

import static com.pnet.TestingUtils.getLocalTestsDataPath;
import static com.pnet.util.PNSystem.UnsupportedOperation;
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

            @Override
            public void onMessageSendingFailedFlood(long chatId) {
                UnsupportedOperation(this);
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
        List<Message> chatHistory = telega.getUserChatHistory(id,0, 0, 10);
        Assertions.assertNotEquals(0, chatHistory.size());
    }
    @Test
    void givenTEST_OUTBOUND_USER_ID_WhenGetChatHistory_ThenHistorySizeNotEquals0() throws Exception {
        List<Message> chatHistory = telega.getUserChatHistory(Config.TEST_OUTBOUND_USER_ID, 0, 0, 10);
        Assertions.assertNotEquals(0, chatHistory.size());
    }

    @Test
    void givenContactInChatHistoryMessage_WhenCallIsUserRegularNotScam_ThenTrue() throws Exception, TdApiException {
        boolean userRegularNotScam; //=
                //telega.isUserRegularNotScam(Config.TEST_OUTBOUND_USER_ID_FROM_CHATMSG_CONTACT, "");

        long chatId = telega.checkChatInviteLink(Config.TEST_OUTBOUND_CHAT_INVITELINK).chatId;

        userRegularNotScam =
                telega.isUserRegularNotScam(Config.TEST_OUTBOUND_USER_ID_FROM_CHATMSG_CONTACT, "");

        TdApi.Messages chatHistory = telega.getChatHistory(
                //getSuperGroupId(telega.checkChatInviteLink(Config.TEST_OUTBOUND_CHAT_INVITELINK)),
                chatId,
                0, 0, 100);

        userRegularNotScam =
                telega.isUserRegularNotScam(Config.TEST_OUTBOUND_USER_ID_FROM_CHATMSG_CONTACT, "");

        TdApi.Contact contact=null;
        for (int i = 0; i <chatHistory.messages.length; i++) {
            contact = tryContactFromMessage(chatHistory.messages[i]);
            if(null!=contact)
                break;
        }

        Assertions.assertTrue(telega.isUserRegularNotScam(contact.userId, ""));
    }

    private TdApi.Contact tryContactFromMessage(TdApi.Message message) {
        if(message.content.getConstructor()==TdApi.MessageContact.CONSTRUCTOR)
            return ((TdApi.MessageContact)message.content).contact;

        return null;
    }

    private int getSuperGroupId(TdApi.ChatInviteLinkInfo checkChatInviteLink) {
        switch (checkChatInviteLink.type.getConstructor()) {
            case TdApi.ChatTypeSupergroup.CONSTRUCTOR:
                return ((TdApi.ChatTypeSupergroup) checkChatInviteLink.type).supergroupId;
            case TdApi.ChatTypeBasicGroup.CONSTRUCTOR:
                return ((TdApi.ChatTypeBasicGroup) checkChatInviteLink.type).basicGroupId;
        }
        UnsupportedOperation(checkChatInviteLink.type.getClass().getTypeName());
        return 0;
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
                telega.checkChatInviteLink(Config.TEST_OUTBOUND_CHAT_INVITELINK).chatId);
    }

    @Test
    void checkChatInviteLink() throws Exception {
        Assertions.assertNotEquals(0, telega.checkChatInviteLink(Config.TEST_OUTBOUND_CHAT_INVITELINK));
    }

    @Disabled
    @Test
    void isUserRegularNotScam() throws Exception {
        Assertions.assertTrue(telega.isUserRegularNotScam(924113501, Config.TEST_CHAT_NAME));
    }

    @Disabled
    @Test
    void scratch() throws Exception, TdApiException {
        TdApi.ChatInviteLinkInfo chatInviteLinkInfo = telega.checkChatInviteLink(Config.superGroupLinks.get(0));
        telega.isUserRegularNotScam(chatInviteLinkInfo.memberUserIds[1], "");
        int id = Math.toIntExact(chatInviteLinkInfo.chatId);
        List<Integer> supergroupMembers = telega.getSupergroupMembers(chatInviteLinkInfo.title);
        supergroupMembers = telega.getSupergroupMembers(id, 0, 200);
    }

    @Disabled
    @Test
    void scratch2(){
        User user = telega.tryObtainUser(Config.TEST_USERID_400ERROR, Config.TMP_VICTIM_GROUP_NAME);
    }

    @Disabled
    @Test
    void scratch3() throws Exception, TdApiException {
        //Успешно получает данные только первых 10 пользователей по ссылке на чат
        TdApi.ChatInviteLinkInfo chatInviteLinkInfo = telega.checkChatInviteLink(
                Config.superGroupLinks.get(0));
        //telega.isUserRegularNotScam(chatInviteLinkInfo.memberUserIds[2], "");
        int id=0;
        //int id = Math.toIntExact(chatInviteLinkInfo.chatId);
        id=((TdApi.ChatTypeSupergroup)chatInviteLinkInfo.type).supergroupId;
        //List<Integer> supergroupMembers = telega.getSupergroupMembers(chatInviteLinkInfo.title);
        //List<Integer> supergroupMembers2 = telega.getSupergroupMembers(id, 0, 200);
        for (int memberUserId : chatInviteLinkInfo.memberUserIds) {
            telega.isUserRegularNotScam(memberUserId, "");//chatInviteLinkInfo.title);
        }
    }

}