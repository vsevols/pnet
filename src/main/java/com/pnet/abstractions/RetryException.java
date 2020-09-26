package com.pnet.abstractions;

import com.pnet.telega.TdApiException;

public class RetryException extends Exception {
    public RetryException(Throwable cause) {
        super(cause);
    }
}
