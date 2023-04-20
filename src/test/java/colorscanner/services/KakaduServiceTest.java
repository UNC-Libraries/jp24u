package colorscanner.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class KakaduServiceTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private KakaduService service;
    private ColorFieldsService colorFieldsService;
    private TemporaryImageService temporaryImageService;

    @BeforeEach
    public void setup() throws Exception {
        System.setOut(new PrintStream(outputStreamCaptor));

        colorFieldsService = new ColorFieldsService();
        temporaryImageService = new TemporaryImageService();
        service = new KakaduService();
        service.setColorFieldsService(colorFieldsService);
        service.setTemporaryImageService(temporaryImageService);
    }

    @Test
    public void testRetrieveColorSpace() throws Exception {
        // exif ColorSpace is null, exif PhotometricInterpretation is gray
        String testFile = "src/test/resources/P0024_0066.tif";
        String colorSpace = service.getColorSpace(testFile);
        assertEquals("Gray", colorSpace);
    }

    @Test
    public void testKakaduKduCompress() throws Exception {
        String testFile = "src/test/resources/E101_F8_0112.tif";
        service.kduCompress(testFile);

        assertTrue(Files.exists(Paths.get("src/test/resources/E101_F8_0112.jp2")));

        Files.deleteIfExists(Paths.get("src/test/resources/E101_F8_0112.jp2"));
    }

    @Test
    public void testKakaduKduCompressGrayscale() throws Exception {
        String testFile = "src/test/resources/P0024_0103_01.tif";
        service.kduCompress(testFile);

        assertTrue(Files.exists(Paths.get("src/test/resources/P0024_0103_01.jp2")));

        Files.deleteIfExists(Paths.get("src/test/resources/P0024_0103_01.jp2"));
    }

    @Test
    public void testKakaduKduCompressCmyk() throws Exception {
        String testFile = "src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif";
        service.kduCompress(testFile);

        assertTrue(Files.exists(Paths.get("tmp/OP20459_1_TremorsKelleyandtheCowboys.tif.jpg")));
        assertTrue(Files.exists(Paths.get("src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.jp2")));

        Files.deleteIfExists(Paths.get("tmp/OP20459_1_TremorsKelleyandtheCowboys.tif.jpg"));
        Files.deleteIfExists(Paths.get("src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.jp2"));
    }

    @Test
    public void testKduCompressPsd() throws Exception {
        String testFile = "src/test/resources/17.psd";
        service.kduCompress(testFile);
        assertTrue(Files.exists(Paths.get("src/test/resources/17.jp2")));

        //service.kduCompress(testFile, tmpFolder.toString());
        //service.kduCompress(testFile, "src/test/resources");

        //assertTrue(Files.exists(Paths.get(tmpFolder + "/17.jp2")));
    }

    @Disabled
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

        Files.deleteIfExists(Paths.get("src/test/resources/E101_F8_0112.jp2"));
        Files.deleteIfExists(Paths.get("src/test/resources/P0024_0066.jp2"));
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
