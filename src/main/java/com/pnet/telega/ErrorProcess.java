package com.pnet.telega;

import it.tdlight.tdlib.TdApi;

public class ErrorProcess {
    public ErrorProcess(TdApi.Object object) throws TdApiException {
        if(TdApi.Error.CONSTRUCTOR==object.getConstructor())
            throw new TdApiException((TdApi.Error) object);
    }
}
