import com.pnet.Router;
import it.tdlight.tdlight.utils.CantLoadLibrary;

public class Main {
    public static void main(String[] args) throws CantLoadLibrary {
        Router router = new Router();
        router.Init();
        router.run();
    }

}
