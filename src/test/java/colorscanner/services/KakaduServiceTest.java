package colorscanner.services;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class KakaduServiceTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private KakaduService service;
    private ColorFieldsService colorFieldsService;

    @Before
    public void setup() throws Exception {
        System.setOut(new PrintStream(outputStreamCaptor));

        colorFieldsService = new ColorFieldsService();
        service = new KakaduService();
        service.setColorFieldsService(colorFieldsService);
    }

    @Test
    public void testRetrieveColorSpace() throws Exception {
        String testFile = "src/test/resources/P0024_0066.tif";
        String colorSpace = service.getColorSpace(testFile);
        assertEquals("null", colorSpace);
    }

    @Test
    public void testKakaduKduCompress() throws Exception {
        String testFile = "src/test/resources/E101_F8_0112.tif";
        service.kduCompress(testFile);

        assertTrue(Files.exists(Paths.get("src/test/resources/E101_F8_0112.jp2")));
    }

    @Test
    public void testKakaduKduCompressGrayscale() throws Exception {
        String testFile = "src/test/resources/P0024_0103_01.tif";
        service.kduCompress(testFile);

        assertTrue(Files.exists(Paths.get("src/test/resources/P0024_0103_01.jp2")));
    }

    @Test
    public void testKakaduKduCompressFail() throws Exception {
        String testFile = "src/test/resources/test_input.txt";

        try {
            service.kduCompress(testFile);
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("File format could not be determined"));
        }
    }

    @Test
    public void testListOfFilesKduCompress() throws Exception {
        String testFile = "src/test/resources/test_input.txt";
        service.fileListKduCompress(testFile);

        assertTrue(Files.exists(Paths.get("src/test/resources/E101_F8_0112.jp2")));
        assertTrue(Files.exists(Paths.get("src/test/resources/P0024_0066.jp2")));
    }

    @Test
    public void testListofFilesWithNonexistentFileKduCompress() throws Exception {
        String testFile = "src/test/resources/test_input_fail.txt";

        try {
            service.fileListKduCompress(testFile);
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("src/test/resources/test.tif does not exist. " +
                    "Not processing file list further."));
        }
    }
}
