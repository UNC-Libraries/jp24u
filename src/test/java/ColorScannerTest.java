import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

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
    public void testFileSize() {
        String testFile = "src/test/resources/lorem_ipsum.txt";
        String[] args = new String[1];
        args[0] = testFile;

        colorScanner.main(args);
        assertEquals("File size: 3278", outputStreamCaptor.toString().trim());
    }

    @Test
    public void testMultipleArgumentsFail() {
        String[] args = new String[2];
        args[0] = "src/test/resources/lorem_ipsum.txt";
        args[1] = "test";

        ColorScanner.main(args);
        assertEquals("Error: File does not exist.", outputStreamCaptor.toString().trim());
    }

    @Test
    public void testNoFileArgFail() {
        String[] args = new String[1];
        args[0] = null;

        ColorScanner.main(args);
        assertEquals("Error: File does not exist.", outputStreamCaptor.toString().trim());
    }

}
