package com.pnet.routing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pnet.PartyId;
import com.pnet.abstractions.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class MessageImpl implements Message {
    private final long id;
    @JsonProperty("outgoing")
    private final boolean isOutgoing;
    public final int senderUserId;
    public final long chatId;
    public final String text;
}
