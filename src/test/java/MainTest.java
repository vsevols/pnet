import com.pnet.ConfigService;

import com.pnet.Debug;
import com.pnet.TestingUtils;
import com.pnet.secure.Config;
import it.tdlight.tdlight.utils.CantLoadLibrary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class MainTest {

    @BeforeEach
    void setUp() {
        Debug.debug=new Debug(true);
    }

    @AfterEach
    void tearDown() {
    }

    @Disabled //test for debug
    @Test
    void mainIncomingMessageEmulateDontRealySendMessages() throws Exception {
        Debug.debug.dontReallyReproduceMessages =true;
        setTestConfig("mainIncomingMessageEmulate.json");
        Main.main(new String[0]);
    }

    private void setTestConfig(String etalonConfigName) throws IOException {
        String localTestsDataPath = TestingUtils.getLocalTestsDataPath("");
        Config.setDataDirectory(localTestsDataPath);
        File dataFile = new File(ConfigService.getDataFilePath());
        FileCopyUtils.copy(new File(localTestsDataPath+ etalonConfigName),
                dataFile);
    }

    //test for debug
    @Disabled
    @Test
    void emptyConfigDontRealySendMessages() throws Exception {
        Debug.debug.dontReallyReproduceMessages =true;
        //debug.noGreetingMessageTimeout=true;
        setTestConfig(Config.emptyConfig());
        Main.main(new String[0]);
    }

    //test for debug
    @Disabled
    @Test
    void workingConfigCopyDontRealySendMessages() throws Exception {
        Debug.debug.dontReallyReproduceMessages =true;
        //debug.noGreetingMessageTimeout=true;
        setTestConfig(ConfigService.loadConfig());
        Main.main(new String[0]);
    }

    private void setTestConfig(Config config) {
        String localTestsDataPath = TestingUtils.getLocalTestsDataPath("");
        Config.setDataDirectory(localTestsDataPath);
        ConfigService.saveConfig(config);
    }

    @Disabled
    @Test
    public void scratch(){
        int outgoingCount=0;
        int timeoutMinutes;
        timeoutMinutes= (int) (Math.pow(10, ++outgoingCount-1)*9);
        timeoutMinutes= (int) (Math.pow(10, ++outgoingCount-1)*9);
        timeoutMinutes= (int) (Math.pow(10, ++outgoingCount-1)*9);
        timeoutMinutes= (int) (Math.pow(10, ++outgoingCount-1)*9);
    }
}