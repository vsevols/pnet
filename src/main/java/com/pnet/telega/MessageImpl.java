package com.pnet.telega;

import com.pnet.PartyId;
import com.pnet.abstractions.Message;
import it.tdlight.tdlib.TdApi;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class MessageImpl implements Message {
    //@Delegate //Как делигировать в пулбличные поля message?
    private final TdApi.Message message;

    @Override
    public long getId() {
        return message.id;
    }

    @Override
    public boolean isOutgoing() {
        return message.isOutgoing;
    }

    @Override
    public int getSenderUserId() {
        return message.senderUserId;
    }

    @Override
    public long getChatId() {
        return message.chatId;
    }

    @Override
    public String getText() {
        return ((TdApi.MessageText)message.content).text.text;
    }

    @Override
    public int getDate() {
        return message.date;
    }
}
