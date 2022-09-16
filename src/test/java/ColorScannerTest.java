import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author krwong
 */
public class ColorScannerTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    ColorScanner colorScanner = new ColorScanner();

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    public void testFileSize() throws Exception {
        String testFile = "src/test/resources/lorem_ipsum.txt";
        Path testPath = Paths.get(testFile);
        String[] args = new String[1];
        args[0] = testFile;

        colorScanner.main(args);
        assertEquals("File size: 3278", outputStreamCaptor.toString().trim());
        assertTrue(Files.exists(testPath));
    }

    @Test
    public void testMultipleArgumentsFail() throws Exception {
        String[] args = new String[2];
        args[0] = "src/test/resources/lorem_ipsum.txt";
        args[1] = "test";

        ColorScanner.main(args);
        assertEquals("Error: File does not exist.", outputStreamCaptor.toString().trim());
    }

    @Test
    public void testNoFileArgFail() throws Exception {
        String[] args = new String[1];
        args[0] = " ";

        ColorScanner.main(args);
        assertEquals("Error: File does not exist.", outputStreamCaptor.toString().trim());
    }

    @Test
    public void testFileDoesNotExist() throws Exception {
        String testFile = "src/test/resources/test.txt";
        Path testPath = Paths.get(testFile);
        String[] args = new String[1];
        args[0] = testFile;

        colorScanner.main(args);
        assertFalse(Files.exists(testPath));
    }

}
