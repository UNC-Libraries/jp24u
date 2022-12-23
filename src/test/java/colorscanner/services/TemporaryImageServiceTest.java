package colorscanner.services;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TemporaryImageServiceTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private TemporaryImageService service;

    @Before
    public void setup() throws Exception {
        System.setOut(new PrintStream(outputStreamCaptor));

        service = new TemporaryImageService();
    }

    @Test
    public void testConvertImageWithIccProfile() throws Exception {
        String testFile = "src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif";
        service.convertImage(testFile);

        assertTrue(Files.exists(Paths.get("src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif.jpg")));
    }

    @Ignore("testFile is 155.6MB, too big to add to github") //@Disable
    @Test
    public void testConvertImageWithoutIccProfile() throws Exception {
        String testFile = "src/test/resources/Surgery.tif";
        service.convertImage(testFile);

        assertTrue(Files.exists(Paths.get("tmp/Surgery.tif.jpg")));
    }

    @Test
    public void testConvertImageWithMultipleTemporaryImages() throws Exception {
        String testFile = "src/test/resources/SAAACAM-HopeHouse_transparency_with-title_merged.tif";
        service.convertImage(testFile);

        assertTrue(Files.exists(Paths.get("tmp/SAAACAM-HopeHouse_transparency_with-title_merged.tif-0.jpg")));
        assertTrue(Files.exists(Paths.get("tmp/SAAACAM-HopeHouse_transparency_with-title_merged.tif-1.jpg")));
    }

    @Test
    public void testTmpImageFileDirDeleted() throws Exception {
        String testFile = "src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif";
        service.convertImage(testFile);
        service.deleteTmpImageFilesDir();

        assertFalse(Files.exists(Paths.get("tmp")));
    }

}
