package com.pnet;

import it.tdlight.tdlib.TdApi;

public interface ResultHandler {
    boolean onResult(TdApi.Object object);
}
