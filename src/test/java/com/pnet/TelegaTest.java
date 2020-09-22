package com.pnet;

import com.pnet.secure.Config;
import com.pnet.telega.TdApiException;
import it.tdlight.tdlib.TdApi;
import it.tdlight.tdlight.utils.CantLoadLibrary;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TelegaTest {

    @Test
    public void TestSendMessageAndOnMessage() throws CantLoadLibrary, TdApiException {
        Telega telega = new Telega();
        telega.init();
        final String MESSAGE_TEXT =
                "TestSendMessageAndOnMessage() message text";

        final boolean[] isPassed = new boolean[1];

        telega.onMessage= new OnMessageHandler() {
            @Override
            public void onMessage(Message msg) {
                //TODO: request and check partyId (phone)
                //telega.getPartyId(msg.senderUserId)
                if(//msg.phone.value.equals(Config.ACCOUNT_PHONE)&&
                        msg.text.equals(MESSAGE_TEXT))
                    isPassed[0] =true;
            }
        };
        telega.sendMessage(Config.ACCOUNT_PHONE, MESSAGE_TEXT);
        while (!isPassed[0])
            telega.process(1000);
    }

}