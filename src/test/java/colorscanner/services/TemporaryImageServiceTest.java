package colorscanner.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemporaryImageServiceTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private ColorFieldsService colorFieldsService;
    private TemporaryImageService service;

    @BeforeEach
    public void setup() throws Exception {
        System.setOut(new PrintStream(outputStreamCaptor));

        colorFieldsService = new ColorFieldsService();
        service = new TemporaryImageService();
    }

    @Test
    public void testConvertImageWithIccProfile() throws Exception {
        String testFile = "src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif";
        service.convertImage(testFile);

        assertTrue(Files.exists(Paths.get(service.TMP_FILES_DIR +
                "/OP20459_1_TremorsKelleyandtheCowboys.tif.jpg")));
    }

    @Disabled("testFile is 155.6MB, too big to add to github")
    @Test
    public void testConvertImageWithoutIccProfile() throws Exception {
        String testFile = "src/test/resources/Surgery.tif";
        service.convertImage(testFile);

        assertTrue(Files.exists(Paths.get(service.TMP_FILES_DIR + "/Surgery.tif.jpg")));
    }

    @Test
    public void testConvertPsdToTiff() throws Exception {
        String testFile = "src/test/resources/17.psd";
        String tempTif = service.TMP_FILES_DIR + "/17.psd.tif";
        String tifExifData = "DateTimeOriginal:2008:02:07 13:05:19\tDateTimeDigitized:2008:02:07 13:05:19\t" +
                "ICCProfileName:sRGB IEC61966-2.1\tColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 1228x1818;Channels: srgb;Bit-depth: 8;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: 8bim,exif,icc,iptc,xmp;ICC Profile: sRGB IEC61966-2.1;ICM Profile: ;\"";

        service.convertPsd(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testConvertImageWithMultipleTemporaryImages() throws Exception {
        String testFile = "src/test/resources/SAAACAM-HopeHouse_transparency_with-title_merged.tif";
        service.convertImage(testFile);

        assertTrue(Files.exists(Paths.get(service.TMP_FILES_DIR +
                "/SAAACAM-HopeHouse_transparency_with-title_merged.tif-0.jpg")));
        assertTrue(Files.exists(Paths.get(service.TMP_FILES_DIR +
                "/SAAACAM-HopeHouse_transparency_with-title_merged.tif-1.jpg")));
    }

    @Test
    public void testTmpImageFileDirDeleted() throws Exception {
        String testFile = "src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif";
        service.convertImage(testFile);
        service.deleteTmpImageFilesDir();

        assertFalse(Files.exists(service.TMP_FILES_DIR));
    }

}
