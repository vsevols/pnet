import Exceptions.StormException;
import lombok.Getter;
import lombok.Setter;
import org.telegram.api.TLConfig;
import org.telegram.api.engine.storage.AbsApiState;
import org.telegram.mtproto.pq.Authorizer;
import org.telegram.mtproto.pq.PqAuth;
import org.telegram.mtproto.state.AbsMTProtoState;
import org.telegram.mtproto.state.ConnectionInfo;
import com.droidkit.actors.ActorCreator;
import org.telegram.mtproto.state.KnownSalt;

import java.util.HashMap;

public class ApiState implements AbsApiState {
    @Getter
    @Setter
    private int primaryDc;

    private HashMap<Integer, Boolean> isAuth = new HashMap<Integer, Boolean>();

    @Override
    public boolean isAuthenticated(int dcId) {
        if (isAuth.containsKey(dcId)) {
            return isAuth.get(dcId);
        }
        return false;
    }

    @Override
    public void setAuthenticated(int dcId, boolean auth) {
        //TODO: implement
        StormException.UnsupportedOperation(this);

    }

    @Override
    public void updateSettings(TLConfig config) {
        //TODO: implement
        StormException.UnsupportedOperation(this);

    }

    @Override
    public byte[] getAuthKey(int dcId) {
        return generateKeys();
    }

    public byte[] generateKeys(){
        boolean isTest = false;
        Authorizer authorizer = new Authorizer();
        HashMap<Integer, ConnectionInfo[]> connections = new HashMap<Integer, ConnectionInfo[]>();
        connections.put(1, new ConnectionInfo[]{
                new ConnectionInfo(1, 0, isTest ? "149.154.175.10" : "149.154.175.50", 443)
        });
        PqAuth pqAuth = authorizer.doAuth(connections.get(1));
        return pqAuth.getAuthKey();
    }

    @Override
    public void putAuthKey(int dcId, byte[] key) {
        //TODO: implement
        StormException.UnsupportedOperation(this);

    }

    @Override
    public ConnectionInfo[] getAvailableConnections(int dcId) {
        return new ConnectionInfo[0];
    }

    @Override
    public AbsMTProtoState getMtProtoState(int dcId) {
        return new AbsMTProtoState() {
            private KnownSalt[] knownSalts = new KnownSalt[0];

            @Override
            public byte[] getAuthKey() {
                return ApiState.this.getAuthKey(dcId);
            }

            @Override
            public ConnectionInfo[] getAvailableConnections() {
                return ApiState.this.getAvailableConnections(dcId);
            }

            @Override
            public KnownSalt[] readKnownSalts() {
                return knownSalts;
            }

            @Override
            protected void writeKnownSalts(KnownSalt[] salts) {
                knownSalts = salts;
            }
        };

    }

    @Override
    public void resetAuth() {
        //TODO: implement
        StormException.UnsupportedOperation(this);

    }

    @Override
    public void reset() {
        //TODO: implement
        StormException.UnsupportedOperation(this);

    }

}
