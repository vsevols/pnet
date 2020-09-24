package com.pnet.routing;

import com.pnet.abstractions.Message;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@Data
@RequiredArgsConstructor
public class RoutingMessage implements Message {
    @Delegate
    private final Message msg;
    private final boolean isGreeting;
    private int copiesSent;
}
