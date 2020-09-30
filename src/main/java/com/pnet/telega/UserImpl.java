package com.pnet.telega;

import com.pnet.abstractions.User;
import it.tdlight.tdlib.TdApi;
import lombok.RequiredArgsConstructor;
import lombok.ToString;


@ToString
@RequiredArgsConstructor
public class UserImpl implements User {
    private final TdApi.User user;

    public String getFirstName() {
        return user.firstName;
    }

    public String getLastName() {
        return user.lastName;
    }

    public String getUsername() {
        return user.username;
    }
}
