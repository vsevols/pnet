package com.pnet;

import it.tdlight.tdlib.TdApi;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Message extends TdApi.Message {
    public final PartyId phone;
    public final String text;
    Message(TdApi.Message msg){
        phone=null;
        TdApi.MessageText text = (TdApi.MessageText) msg.content;
        this.text=text.text.text;
    }
}
