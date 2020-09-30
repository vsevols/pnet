package com.pnet.telega;

import com.pnet.abstractions.User;
import it.tdlight.tdlib.TdApi;
import lombok.RequiredArgsConstructor;
import lombok.ToString;


@ToString
@RequiredArgsConstructor
public class UserImpl implements User {
    private final TdApi.User user;

    @Override
    public String getFirstName() {
        return user.firstName;
    }

    @Override
    public String getLastName() {
        return user.lastName;
    }

    @Override
    public String getUsername() {
        return user.username;
    }
}
