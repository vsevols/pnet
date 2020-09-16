package com.pnet;

import it.tdlight.tdlib.TdApi;
import it.tdlight.tdlight.Client;
import it.tdlight.tdlight.Request;
import it.tdlight.tdlight.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
public class TelegaClient extends Client {
    private final ResultHandler resultHandler;
    @Getter
    private double syncPeriod =1000;
    public double setSyncPeriod(double value)
    {
        double prevPeriod=syncPeriod;
        syncPeriod=value;
        return prevPeriod;
    }

    public void send(TdApi.Function function) {
        super.send(new Request(function.getConstructor(), function));
    }

    public void send(TdApi.Function function, ResultHandler resultHandler) throws TimeoutException {
        send(function);
        if(null==resultHandler)
            return;
        Response response;
        do {
            response = receive(syncPeriod);
            if(null==response)
                throw new TimeoutException(function.toString());
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
