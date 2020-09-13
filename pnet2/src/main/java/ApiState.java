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

import java.util.HashMap;

public class ApiState implements AbsApiState {
    @Getter
    @Setter
    private int primaryDc;

    @Override
    public boolean isAuthenticated(int dcId) {
        //TODO: implement
        StormException.UnsupportedOperation(this);
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
        Authorizer authorizer = new Authorizer();
        HashMap<Integer, ConnectionInfo[]> connections = new HashMap<Integer, ConnectionInfo[]>();
        connections.put(1, new ConnectionInfo[]{
                new ConnectionInfo(1, 0, "149.154.167.40", 443) // Test
                //new ConnectionInfo(1, 0, "173.240.5.1", 443) // Production
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
        //TODO: implement
        StormException.UnsupportedOperation(this);
        return new ConnectionInfo[0];
    }

    @Override
    public AbsMTProtoState getMtProtoState(int dcId) {
        //TODO: implement
        StormException.UnsupportedOperation(this);
        return null;
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
