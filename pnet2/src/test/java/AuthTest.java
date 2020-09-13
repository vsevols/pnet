import junit.framework.TestCase;
import org.junit.Test;
import org.telegram.mtproto.pq.Authorizer;
import org.telegram.mtproto.pq.PqAuth;
import org.telegram.mtproto.state.ConnectionInfo;

import java.util.HashMap;

public class AuthTest {

    @Test
    public void generateKeys(){
        Authorizer authorizer = new Authorizer();
        HashMap<Integer, ConnectionInfo[]> connections = new HashMap<Integer, ConnectionInfo[]>();
        connections.put(1, new ConnectionInfo[]{
                new ConnectionInfo(1, 0, "149.154.167.40", 443) // Test
        //new ConnectionInfo(1, 0, "173.240.5.1", 443) // Production
        });
        PqAuth pqAuth = authorizer.doAuth(connections.get(1));
        pqAuth.getAuthKey();
    }

}