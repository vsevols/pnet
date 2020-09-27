import com.pnet.ConfigService;
import static com.pnet.Debug.*;

import com.pnet.Debug;
import com.pnet.secure.Config;
import it.tdlight.tdlight.utils.CantLoadLibrary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

class MainTest {

    @BeforeEach
    void setUp() {
        Debug.debug=new Debug();
    }

    @AfterEach
    void tearDown() {
        Debug.debug=new Debug();
    }

    @Disabled //test for debug
    @Test
    void mainIncomingMessageEmulateDontRealySendMessages() throws IOException, CantLoadLibrary {
        setTestConfig("mainIncomingMessageEmulate.json");
        Debug.debug.dontReallySendMessages=true;
        Main.main(new String[0]);
    }

    private void setTestConfig(String etalonConfigName) throws IOException {
        String localTestsDataPath = getLocalTestsDataPath("");
        Config.setDataDirectory(localTestsDataPath);
        File dataFile = new File(ConfigService.getDataFilePath());
        FileCopyUtils.copy(new File(localTestsDataPath+ etalonConfigName),
                dataFile);
    }

    //test for debug
    @Disabled
    @Test
    void emptyConfigDontRealySendMessages() throws IOException, CantLoadLibrary {
        Debug.debug.dontReallySendMessages=true;
        //debug.noGreetingMessageTimeout=true;
        setTestConfig(Config.emptyConfig());
        Main.main(new String[0]);
    }

    //test for debug
    @Disabled
    @Test
    void workingConfigCopyDontRealySendMessages() throws IOException, CantLoadLibrary {
        Debug.debug.dontReallySendMessages=true;
        //debug.noGreetingMessageTimeout=true;
        setTestConfig(ConfigService.loadConfig());
        Main.main(new String[0]);
    }

    private void setTestConfig(Config config) {
        String localTestsDataPath = getLocalTestsDataPath("");
        Config.setDataDirectory(localTestsDataPath);
        ConfigService.saveConfig(config);
    }

    private String getLocalTestsDataPath(String path) {
        return Config.toDataPath("tests/"+path);
    }

    @Disabled
    @Test
    public void scratch(){
        LocalDateTime till = LocalDateTime.now().plusNanos(new Long(Integer.MAX_VALUE) * 1000);
    }
}