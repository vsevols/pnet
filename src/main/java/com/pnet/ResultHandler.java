package com.pnet;

import com.pnet.telega.TdApiException;
import it.tdlight.tdlib.TdApi;

public interface ResultHandler {
    boolean onResult(TdApi.Object object) throws TdApiException;
}
