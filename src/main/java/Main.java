import it.tdlight.tdlight.Client;
import it.tdlight.tdlight.Init;
import it.tdlight.tdlight.Log;
import it.tdlight.tdlight.utils.CantLoadLibrary;

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
    }
}
