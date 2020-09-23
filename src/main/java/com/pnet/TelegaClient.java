package com.pnet;

import com.pnet.telega.TdApiException;
import com.pnet.telega.TypedResultHandler;
import it.tdlight.tdlib.TdApi;
import it.tdlight.tdlight.Client;
import it.tdlight.tdlight.Request;
import it.tdlight.tdlight.Response;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TelegaClient extends Client {
    private final double SYNC_RECEIVE_PERIOD =1000;
    private final ResultHandler resultHandler;

    public void send(TdApi.Function function) {
        super.send(new Request(function.getConstructor(), function));
    }

    public void send(TdApi.Function function, ResultHandler resultHandler) throws TdApiException {
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

    void processUpdates(boolean canThrow) throws TdApiException {
        Response response =null;
        do {
            response = receive(0);
            if(null!=response) {
                try {
                    resultHandler.onResult(response.getObject());
                } catch (TdApiException e) {
                    if(canThrow)
                        throw e;
                    e.printStackTrace();
                }
            }
        }while(null!=response);
    }

    public void processUpdates() {
        try {
            processUpdates(true);
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
}
