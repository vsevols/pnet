import Exceptions.StormException;
import org.telegram.api.TLConfig;
import org.telegram.api.engine.storage.AbsApiState;
import org.telegram.mtproto.state.AbsMTProtoState;
import org.telegram.mtproto.state.ConnectionInfo;

public class ApiState implements AbsApiState {
    @Override
    public int getPrimaryDc() {
        //TODO: implement
        StormException.UnsupportedOperation(this);
        return 0;
    }

    @Override
    public void setPrimaryDc(int dc) {
        //TODO: implement
        StormException.UnsupportedOperation(this);

    }

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
        //TODO: implement
        StormException.UnsupportedOperation(this);
        return new byte[0];
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
