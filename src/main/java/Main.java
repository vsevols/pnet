import com.pnet.Router;
import it.tdlight.tdlight.utils.CantLoadLibrary;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws CantLoadLibrary, IOException {
        Router router = new Router();
        router.Init();
        router.run();
    }

}
