package com.pnet;

import com.pnet.telega.TdApiException;
import com.pnet.telega.TypedResultHandler;
import it.tdlight.tdlib.TdApi;
import it.tdlight.tdlight.Client;
import it.tdlight.tdlight.Request;
import it.tdlight.tdlight.Response;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@AllArgsConstructor
public class TelegaClient extends Client {
    private final double SYNC_RECEIVE_PERIOD =1000;
    private ReceiveHandler defaultReceiveHandler;

    public void send(TdApi.Function function) {
        super.send(new Request(function.getConstructor(), function));
    }

    public void send(TdApi.Function function, ReceiveHandler resultHandler) throws TdApiException {
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
                this.defaultReceiveHandler.onResult(response.getObject());
            else return;
        }while(true);
    }

    public static TelegaClient getClient(TelegaClient client, ReceiveHandler receiveHandler) {
        //Singleton workaround for: Can't lock file td.binlog
        if(null==client) {
            client = new TelegaClient(receiveHandler);
            client.initializeClient();
        }else
            client.defaultReceiveHandler = receiveHandler;

        return client;
    }

    void processReceive(boolean canThrow, int timeoutMs, ReceiveHandler receiveHandler) throws TdApiException {
        LocalDateTime till = LocalDateTime.now().plusNanos(new Long(timeoutMs) * 1000000);
        Response response =null;
        boolean wasReceived=false;
        do {
            long between = ChronoUnit.SECONDS.between(LocalDateTime.now(), till);
            response = receive(wasReceived?0: between>0?between:0);
            if(null!=response) {
                wasReceived=true;
                try {
                    receiveHandler.onResult(response.getObject());
                } catch (TdApiException e) {
                    if(canThrow)
                        throw e;
                    e.printStackTrace();
                }
            }
        }while(null!=response||(!wasReceived&&till.isAfter(LocalDateTime.now())));
    }

    public void processReceive(int timeOutMs, ReceiveHandler receiveHandler) {
        try {
            processReceive(true, timeOutMs, receiveHandler);
        } catch (TdApiException e) {
            e.printStackTrace();
        }
    }

    public <T extends TdApi.Object> T syncRequest(TdApi.Function request, T typeByObject) throws TdApiException {
        final TdApi.Object[] result = new TdApi.Object[1];
        send(request, new TypedResultHandler<T>(typeByObject) {
            @Override
            protected void onTypedResult(T object) {
                result[0] = object;
            }
        });

        return (T) result[0];
    }

    public void processUpdates(int timeOutMs) {
        processReceive(timeOutMs, defaultReceiveHandler);
    }
}
