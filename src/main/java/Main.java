import com.pnet.ResultHandler;
import com.pnet.Telega;
import it.tdlight.tdlib.TdApi;
import it.tdlight.tdlight.*;
import it.tdlight.tdlight.utils.CantLoadLibrary;

import java.io.DataInput;
import java.io.ObjectInput;

public class Main {
    public static void main(String[] args) throws CantLoadLibrary {
        Telega telega = new Telega();
        telega.run();
    }
}
