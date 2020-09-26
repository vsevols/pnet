package com.pnet.telega;

import com.pnet.ConfigService;
import com.pnet.PNSystem;
import it.tdlight.tdlib.TdApi;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.IOException;
import java.time.LocalDateTime;

@Value
public class CachedUser extends TdApi.User {
    boolean exists;
    LocalDateTime lastSeen;
    LocalDateTime cachedMoment=LocalDateTime.now();

    public CachedUser(){
        exists = true;
        lastSeen = LocalDateTime.MIN;
    }

    public CachedUser(int id, LocalDateTime lastSeenNotBefore){
        exists=true;
        lastSeen = lastSeenNotBefore.isAfter(getLastSeenFromSuper())?lastSeenNotBefore:getLastSeenFromSuper();
    }

    public LocalDateTime getLastSeen() {
        return getLastSeenFromSuper().isAfter(lastSeen)?getLastSeenFromSuper():lastSeen;
    }

    public static CachedUser fromUser(TdApi.User user) {
        return fromUser(user, LocalDateTime.MIN);
    }

    public static CachedUser fromUser(TdApi.User user, LocalDateTime lastSeenNotBefore) {
        try {
            //UnrecognizedPropertyException: Unrecognized field "constructor" (class it.tdlight.tdlib.TdApi$UserStatusOffline
            return ConfigService.fromJson(CachedUser.class, ConfigService.toJson(user), true);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return new CachedUser(user.id, lastSeenNotBefore);
        }
    }

    public boolean isExpired(int cacheExpiredMins) {
        return cachedMoment.plusMinutes(cacheExpiredMins).isBefore(LocalDateTime.now());
    }

    private LocalDateTime getLastSeenFromSuper() {
        if(null!=status)
        {
            switch (status.getConstructor()) {
                case TdApi.UserStatusOnline.CONSTRUCTOR:
                case TdApi.UserStatusRecently.CONSTRUCTOR:
                    return LocalDateTime.now();
                case TdApi.UserStatusOffline.CONSTRUCTOR:
                    return PNSystem.unixTimeToLocalDateTime(((TdApi.UserStatusOffline) status).wasOnline);
            }
        }
        return LocalDateTime.MIN;
    }

}
