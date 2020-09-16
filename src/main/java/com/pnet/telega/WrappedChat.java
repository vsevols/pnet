package com.pnet.telega;

import it.tdlight.tdlib.TdApi;

public class WrappedChat extends TdApi.Chat {
    public final TdApi.Chat o;
    public long order;

    public WrappedChat(TdApi.Chat chat) {
        this.o=chat;
    }
}
