package colorscanner;

import colorscanner.services.ColorFieldsService;
import colorscanner.services.KakaduService;
import colorscanner.services.ImagePreproccessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;

import static org.slf4j.LoggerFactory.getLogger;

public class ColorScannerCommandIT {
    private static final Logger log = getLogger(ColorScannerCommandIT.class);
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    protected final PrintStream originalOut = System.out;
    protected final ByteArrayOutputStream out = new ByteArrayOutputStream();
    protected String output;

    protected CommandLine command;

    @TempDir
    public Path tmpFolder;

    private ColorFieldsService colorFieldsService;
    private KakaduService kakaduService;
    private ImagePreproccessingService temporaryImageService;

    @BeforeEach
    public void setup() throws Exception {
        command = new CommandLine(new CLIMain());
        System.setOut(new PrintStream(outputStreamCaptor));

        colorFieldsService = new ColorFieldsService();
        kakaduService = new KakaduService();
        temporaryImageService = new ImagePreproccessingService();
        kakaduService.setColorFieldsService(colorFieldsService);
        kakaduService.setImagePreproccessingService(temporaryImageService);
    }

    @Test
    public void listColorFieldsTest() throws Exception {
        String testFile = "src/test/resources/P0024_0066.tif";
        String[] args = new String[] {
                "colorscanner",
                "list", "-f", testFile
        };

        executeExpectSuccess(args);
    }

    @Test
    public void listColorFieldsLogEfforWithNonexistentFile() throws Exception {
        String testFile = "src/test/resources/test.tif";
        String[] args = new String[] {
                "colorscanner",
                "list", "-f", testFile
        };

        executeExpectSuccess(args);
    }

    @Test
    public void listAllColorFieldsTest() throws Exception {
        String testFile = "src/test/resources/test_input.txt";
        String[] args = new String[] {
                "colorscanner",
                "list_all", "-f", testFile
                };

        executeExpectSuccess(args);
    }

    @Test
    public void listAllColorFieldsWithNonExistentFileFail() throws Exception {
        String testFile = "src/test/resources/test_input_fail.txt";

        String[] args = new String[] {
                "colorscanner",
                "list_all", "-f", testFile
        };

        executeExpectSuccess(args);
    }

    @Test
    public void kakaduKduCompressTest() throws Exception {
        String testFile = "src/test/resources/P0024_0103_01.tif";

        String[] args = new String[] {
                "colorscanner",
                "kdu_compress", "-f", testFile,
                "-o", tmpFolder.toString()
        };

        executeExpectSuccess(args);
    }

    @Disabled
    @Test
    public void kakaduKduCompressFail() throws Exception {
        String testFile = "src/test/resources/test.tif";

        String[] args = new String[] {
                "colorscanner",
                "kdu_compress", "-f", testFile,
                "-o", tmpFolder.toString()
        };

        executeExpectFailure(args);
    }

    @Test
    public void kakaduKduCompressAllTest() throws Exception {
        String testFile = "src/test/resources/test_input.txt";

        String[] args = new String[] {
                "colorscanner",
                "kdu_compress_all", "-f", testFile,
                "-o", tmpFolder.toString()
        };

        executeExpectSuccess(args);
    }

    @Test
    public void kakaduKduCompressAllFail() throws Exception {
        String testFile = "src/test/resources/test_input_fail.txt";

        String[] args = new String[] {
                "colorscanner",
                "kdu_compress_all", "-f", testFile,
                "-o", tmpFolder.toString()
        };

        executeExpectFailure(args);
    }

    protected void executeExpectSuccess(String[] args) {
        int result = command.execute(args);
        output = out.toString();
        if (result != 0) {
            System.setOut(originalOut);
            // Can't see the output from the command without this
            System.out.println(output);
            fail("Expected command to result in success: " + String.join(" ", args) + "\nWith output:\n" + output);
        }
    }

    protected void executeExpectFailure(String[] args) {
        int result = command.execute(args);
        output = out.toString();
        if (result == 0) {
            System.setOut(originalOut);
            log.error(output);
            fail("Expected command to result in failure: " + String.join(" ", args));
        }
    }
}
