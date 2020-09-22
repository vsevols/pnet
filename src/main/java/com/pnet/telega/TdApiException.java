package com.pnet.telega;

import it.tdlight.tdlib.TdApi;
import lombok.Value;

@Value
public class TdApiException extends Throwable {
    private final int code;
    private final String message;

    public TdApiException(TdApi.Error error) {
        code=error.code;
        message=error.message;
    }
}
