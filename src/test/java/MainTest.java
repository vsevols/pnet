import com.pnet.ConfigService;
import com.pnet.secure.Config;
import it.tdlight.tdlight.utils.CantLoadLibrary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;

class MainTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Disabled //test for debug
    @Test
    void mainIncomingMessageEmulate() throws IOException, CantLoadLibrary {
        String localTestsDataPath = getLocalTestsDataPath("");
        Config.setDataDirectory(localTestsDataPath);
        File dataFile = new File(ConfigService.getDataFilePath());
        FileCopyUtils.copy(new File(localTestsDataPath+"mainIncomingMessageEmulate.json"),
                dataFile);
        Main.main(new String[0]);
    }

    private String getLocalTestsDataPath(String path) {
        return Config.toDataPath("tests/"+path);
    }
}