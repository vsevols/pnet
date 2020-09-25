package com.pnet.telega;

import com.pnet.ConfigService;
import com.pnet.secure.Config;
import it.tdlight.tdlib.TdApi;
import lombok.Value;

import java.io.IOException;
import java.time.LocalDateTime;

@Value
public class CachedUser extends TdApi.User {
    boolean exists;
    LocalDateTime cachedMoment=LocalDateTime.now();
    CachedUser(){
        exists=true;
    }
    CachedUser(int id){
        exists=false;
    }
    public static CachedUser fromUser(TdApi.User user) {
        try {
            //UnrecognizedPropertyException: Unrecognized field "constructor" (class it.tdlight.tdlib.TdApi$UserStatusOffline
            return ConfigService.fromJson(CachedUser.class, ConfigService.toJson(user), true);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return new CachedUser(user.id);
        }
    }

    public boolean isExpired(int cacheExpiredMins) {
        return cachedMoment.plusMinutes(cacheExpiredMins).isBefore(LocalDateTime.now());
    }
}
