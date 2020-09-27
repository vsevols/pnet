package com.pnet.telega;

import com.pnet.ReceiveHandler;
import it.tdlight.tdlib.TdApi;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class TypedResultHandler<T extends TdApi.Object> implements ReceiveHandler {
    private final T typeByObject;

    @Override
    public boolean onResult(TdApi.Object object) throws TdApiException {
        if(typeByObject.getConstructor()==object.getConstructor()) {
            onTypedResult((T) object);
            return true;
        }

        new ErrorProcess(object);
        return false;
    }

    protected abstract void onTypedResult(T object);
}
