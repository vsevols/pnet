import com.pnet.Telega;
import com.pnet.secure.Config;
import it.tdlight.tdlight.utils.CantLoadLibrary;

import java.util.concurrent.TimeoutException;

public class Main {
    public static void main(String[] args) throws CantLoadLibrary {
        Telega telega = new Telega();
        telega.init();
        try {
            telega.sendMessage(Config.TEST_PHONE, "test message");
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
