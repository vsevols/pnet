import com.pnet.ResultHandler;
import it.tdlight.tdlib.TdApi;
import it.tdlight.tdlight.*;
import it.tdlight.tdlight.utils.CantLoadLibrary;

import java.io.DataInput;
import java.io.ObjectInput;

public class Main {
    public static void main(String[] args) throws CantLoadLibrary {
        // Initialize TDLight native libraries
        Init.start();

        // Set TDLib log level to 1
        Log.setVerbosityLevel(1);

        // Uncomment this line to print TDLib logs to a file
        // Log.setFilePath("logs" + File.separatorChar + "tdlib.log");

        Client client = new Client();

        // Initialize the TDLib client
        client.initializeClient();

        // Now you can use the client
        client.send(new Request(TdApi.SetTdlibParameters.CONSTRUCTOR,
                new TdApi.SetTdlibParameters(new TdApi.TdlibParameters())));

        new Runnable() {
            @Override
            public void run() {
                while(true){
                    Response response = client.receive(0);
                }
            }
        }
    }
}
