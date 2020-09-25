package com.pnet.telega;

import com.pnet.ConfigService;
import it.tdlight.tdlib.TdApi;
import lombok.Value;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Value
public class CachedUser extends TdApi.User {
    boolean exists;
    LocalDateTime lastSeen;
    LocalDateTime cachedMoment=LocalDateTime.now();
    CachedUser(){
        exists=true;
        lastSeen = LocalDateTime.MIN;
    }
    public CachedUser(int id){
        exists=false;
        lastSeen =LocalDateTime.now();
    }

    public LocalDateTime getLastSeen() {
        return getLastSeenFromSuper().isAfter(lastSeen)?getLastSeenFromSuper():lastSeen;
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

    public LocalDateTime getLastSeenFromSuper() {
        if(null!=status)
        {
            switch (status.getConstructor()) {
                case TdApi.UserStatusOnline.CONSTRUCTOR:
                case TdApi.UserStatusRecently.CONSTRUCTOR:
                    return LocalDateTime.now();
                case TdApi.UserStatusOffline.CONSTRUCTOR:
                    return LocalDateTime.ofInstant(Instant.ofEpochSecond(
                            ((TdApi.UserStatusOffline) status).wasOnline),
                            TimeZone.getDefault().toZoneId()
                    );
            }
        }
        return LocalDateTime.MIN;
    }
}
