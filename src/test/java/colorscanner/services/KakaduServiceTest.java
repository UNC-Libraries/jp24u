package colorscanner.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class KakaduServiceTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @TempDir
    public Path tmpFolder;

    private KakaduService service;
    private ColorFieldsService colorFieldsService;
    private ImagePreproccessingService imagePreproccessingService;

    @BeforeEach
    public void setup() throws Exception {
        System.setOut(new PrintStream(outputStreamCaptor));

        colorFieldsService = new ColorFieldsService();
        imagePreproccessingService = new ImagePreproccessingService();
        service = new KakaduService();
        service.setColorFieldsService(colorFieldsService);
        service.setImagePreproccessingService(imagePreproccessingService);
    }

    @Test
    public void testRetrieveColorSpace() throws Exception {
        // exif ColorSpace is null, exif PhotometricInterpretation is gray
        String testFile = "src/test/resources/P0024_0066.tif";
        String colorSpace = service.getColorSpace(testFile);
        assertEquals("Gray", colorSpace);
    }

    @Test
    public void testKduCompressTiff() throws Exception {
        String testFile = "src/test/resources/E101_F8_0112.tif";
        service.kduCompress(testFile, tmpFolder.toString());

        assertTrue(Files.exists(Paths.get(tmpFolder + "/E101_F8_0112.jp2")));
    }

    @Test
    public void testKduCompressGrayscaleTiff() throws Exception {
        String testFile = "src/test/resources/P0024_0103_01.tif";
        service.kduCompress(testFile, tmpFolder.toString());

        assertTrue(Files.exists(Paths.get(tmpFolder + "/P0024_0103_01.jp2")));
    }

    @Test
    public void testKduCompressCmykTiff() throws Exception {
        String testFile = "src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif";
        service.kduCompress(testFile, tmpFolder.toString());

        assertTrue(Files.exists(Paths.get(imagePreproccessingService.TMP_FILES_DIR +
                "/OP20459_1_TremorsKelleyandtheCowboys.tif.tif")));
        assertTrue(Files.exists(Paths.get(tmpFolder + "/OP20459_1_TremorsKelleyandtheCowboys.jp2")));
    }

    @Test
    public void testKduCompressJpeg() throws Exception {
        String testFile = "src/test/resources/IMG_2377.jpeg";
        service.kduCompress(testFile, tmpFolder.toString());

        assertTrue(Files.exists(Paths.get(tmpFolder + "/IMG_2377.jp2")));
    }

    @Test
    public void testKduCompressPng() throws Exception {
        String testFile = "src/test/resources/schoolphotos1.png";
        service.kduCompress(testFile, tmpFolder.toString());

        assertTrue(Files.exists(Paths.get(tmpFolder + "/schoolphotos1.jp2")));
    }

    @Test
    public void testKduCompressGif() throws Exception {
        String testFile = "src/test/resources/CARTEZOO.GIF";
        service.kduCompress(testFile, tmpFolder.toString());

        assertTrue(Files.exists(Paths.get(tmpFolder + "/CARTEZOO.jp2")));
    }

    @Test
    public void testKduCompressPict() throws Exception {
        String testFile = "src/test/resources/IMG_3444.pct";
        service.kduCompress(testFile, tmpFolder.toString());

        assertTrue(Files.exists(Paths.get(tmpFolder + "/IMG_3444.jp2")));
    }

    @Test
    public void testKduCompressBmp() throws Exception {
        String testFile = "src/test/resources/Wagoner_BW.bmp";
        service.kduCompress(testFile, tmpFolder.toString());

        assertTrue(Files.exists(Paths.get(tmpFolder + "/Wagoner_BW.jp2")));
    }

    @Test
    public void testKduCompressPsd() throws Exception {
        String testFile = "src/test/resources/17.psd";
        service.kduCompress(testFile, tmpFolder.toString());

        assertTrue(Files.exists(Paths.get(tmpFolder + "/17.jp2")));
    }

    @Disabled
    @Test
    public void testKduCompressFail() throws Exception {
        String testFile = "src/test/resources/test_input.txt";

        try {
            service.kduCompress(testFile, tmpFolder.toString());
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("File format could not be determined"));
        }
    }

    @Test
    public void testNonExistentTempFolderFail() throws Exception {
        String testFile = "src/test/resources/E101_F8_0112.tif";

        try {
            service.kduCompress(testFile, "folder");
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("folder does not exist."));
            assertFalse(Files.exists(Paths.get("folder/E101_F8_0112.jp2")));
        }
    }

    @Test
    public void testListOfFilesKduCompress() throws Exception {
        String testFile = "src/test/resources/test_input.txt";
        service.fileListKduCompress(testFile, tmpFolder.toString());

        assertTrue(Files.exists(Paths.get(tmpFolder + "/E101_F8_0112.jp2")));
        assertTrue(Files.exists(Paths.get(tmpFolder + "/P0024_0066.jp2")));
    }

    @Test
    public void testListofFilesWithNonexistentFileKduCompress() throws Exception {
        String testFile = "src/test/resources/test_input_fail.txt";

        try {
            service.fileListKduCompress(testFile, tmpFolder.toString());
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("src/test/resources/test.tif does not exist. " +
                    "Not processing file list further."));
        }
    }
}
