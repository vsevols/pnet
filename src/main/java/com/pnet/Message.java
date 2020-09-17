package com.pnet;

import it.tdlight.tdlib.TdApi;
import lombok.RequiredArgsConstructor;

/**
 * TODO: persp: rename to PNetTdMessage, extract class Message
 */
@RequiredArgsConstructor
public class Message {
    public final PartyId partyId;
    public final String text;
    public final TdApi.Message nativeMessage;
    Message(TdApi.Message nativeMessage){
        partyId = new PartyId("");
        TdApi.MessageText text = (TdApi.MessageText) nativeMessage.content;
        this.text=text.text.text;
        this.nativeMessage = nativeMessage;
    }
}
