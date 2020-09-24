package com.pnet.routing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pnet.abstractions.Message;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@Data
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true) //Для делегированных свойств
public class RoutingMessage implements Message {
    @Delegate
    private final Message msg;
    private final boolean isGreeting;
    private int reproducedCount;
}
