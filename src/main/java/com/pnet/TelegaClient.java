package com.pnet;

import it.tdlight.tdlib.TdApi;
import it.tdlight.tdlight.Client;
import it.tdlight.tdlight.Request;
import it.tdlight.tdlight.Response;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
public class TelegaClient extends Client {
    private final double SYNC_RECEIVE_PERIOD =1000;
    private final ResultHandler resultHandler;

    public void send(TdApi.Function function) {
        super.send(new Request(function.getConstructor(), function));
    }

    public void send(TdApi.Function function, ResultHandler resultHandler) {
        send(function);
        if(null==resultHandler)
            return;
        Response response;
        do {
            response = receive(0);
            if(null==response)
                continue;
              //  throw new TimeoutException(function.toString());
            if (!resultHandler.onResult(response.getObject()))
                this.resultHandler.onResult(response.getObject());
            else return;
        }while(true);
    }

    public static TelegaClient create(ResultHandler resultHandler) {
        TelegaClient client = new TelegaClient(resultHandler);
        client.initializeClient();
        return client;
    }

    void processUpdates() {
        Response response =null;
        do {
            response = receive(0);
            if(null!=response)
                resultHandler.onResult(response.getObject());
        }while(null!=response);
    }
}
