package com.pnet.routing;

import com.pnet.PartyId;
import com.pnet.abstractions.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class MessageImpl implements Message {
    private final long id=0;
    private final boolean isOutgoing=false;
    public final int senderUserId=0;
    public final long chatId=0;
    public final String text;
}
