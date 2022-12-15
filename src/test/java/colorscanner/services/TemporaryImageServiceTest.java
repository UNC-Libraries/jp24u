package colorscanner.services;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    @Test
    public void testConvertImageWithoutIccProfile() throws Exception {
        //this tif is 155.6MB and the other example is about the same size
        //might need to delete this test?
        String testFile = "src/test/resources/Surgery.tif";
        service.convertImage(testFile);

        assertTrue(Files.exists(Paths.get("src/test/resources/Surgery.tif.jpg")));
    }

    @Test
    public void testConvertImageWithMultipleTemporaryImages() throws Exception {
        String testFile = "src/test/resources/SAAACAM-HopeHouse_transparency_with-title_merged.tif";
        service.convertImage(testFile);

        assertTrue(Files.exists(Paths.get("src/test/resources/SAAACAM-HopeHouse_transparency_with" +
                "-title_merged.tif-0.jpg")));
        assertTrue(Files.exists(Paths.get("src/test/resources/SAAACAM-HopeHouse_transparency_with" +
                "-title_merged.tif-1.jpg")));
    }


}
