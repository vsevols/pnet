package com.pnet.telega;

import com.pnet.abstractions.Chat;
import com.pnet.abstractions.Message;
import it.tdlight.tdlib.TdApi;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatImpl implements Chat  {
    private final TdApi.Chat chat;

    @Override
    public long getId() {
        return chat.id;
    }

    @Override
    public Message getLastMessage() {
        return new MessageImpl(chat.lastMessage);
    }
}
