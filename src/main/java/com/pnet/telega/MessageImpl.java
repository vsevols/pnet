package com.pnet.telega;

import com.pnet.PartyId;
import com.pnet.abstractions.Message;
import it.tdlight.tdlib.TdApi;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * TODO: persp: rename to PNetTdMessage, extract class Message
 */
@RequiredArgsConstructor
public class MessageImpl implements Message {
    public final PartyId partyId;
    public final String text;
    private final TdApi.Message message;
    public final int senderUserId;
    @Getter
    private final long id;

    public MessageImpl(TdApi.Message nativeMessage){
        this.message=nativeMessage;
        partyId = new PartyId("");
        TdApi.MessageText text = (TdApi.MessageText) nativeMessage.content;
        this.text=text.text.text;
        senderUserId=nativeMessage.senderUserId;
        throw new UnsupportedOperationException("Отсеять конференции");
    }

    @Override
    public boolean getIsOutgoing() {
        return message.isOutgoing;
    }
}
