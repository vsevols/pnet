package com.pnet;

import it.tdlight.tdlib.TdApi;
import it.tdlight.tdlight.Client;
import it.tdlight.tdlight.Request;

public class TelegaClient extends Client {
    public void send(TdApi.Function function) {
        super.send(new Request(function.getConstructor(), function));
    }

    public void send(TdApi.Function function, Object dummy) {
        send(function);
    }

    public static TelegaClient create() {
        TelegaClient client = new TelegaClient();
        client.initializeClient();
        return client;
    }
}
