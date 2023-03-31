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
    public void testConvertCmykImageWithIccProfile() throws Exception {
        String testFile = "src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif";
        service.convertCmykColorSpace(testFile);
        //colorFieldsService.listFields(testFile);
        //colorFieldsService.listFields("src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif.jpg");

        assertTrue(Files.exists(Paths.get(service.TMP_FILES_DIR +
                "/OP20459_1_TremorsKelleyandtheCowboys.tif.tif")));
    }

    @Disabled("testFile is 155.6MB, too big to add to github")
    @Test
    public void testConvertCmykImageWithoutIccProfile() throws Exception {
        String testFile = "src/test/resources/Surgery.tif";
        service.convertCmykColorSpace(testFile);
        colorFieldsService.listFields(testFile);
        colorFieldsService.listFields("src/test/resources/Surgery.tif.tif");

        assertTrue(Files.exists(Paths.get(service.TMP_FILES_DIR + "/Surgery.tif.tif")));
    }

    @Test
    public void testConvertJpegtoTiff() throws Exception {
        String testFile = "src/test/resources/IMG_2377.jpeg";
        String tempTif = service.TMP_FILES_DIR + "/IMG_2377.jpeg.tif";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\tICCProfileName:Display P3\t" +
                "ColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:YCbCr\t" +
                "MagickIdentify:\"Dimensions: 4032x3024;Channels: srgb;Bit-depth: 8;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: 8bim,icc,iptc;ICC Profile: Display P3;ICM Profile: ;\"";

        service.convertImageFormats(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testConvertPngToTiff() throws Exception {
        String testFile = "src/test/resources/schoolphotos1.png";
        String tempTif = service.TMP_FILES_DIR + "/schoolphotos1.png.tif";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\tICCProfileName:Adobe RGB (1998)\t" +
                "ColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 1300x2000;Channels: srgb;Bit-depth: 16;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: icc;ICC Profile: Adobe RGB (1998);ICM Profile: ;\"";

        service.convertImageFormats(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testConvertGifToTiff() throws Exception {
        String testFile = "src/test/resources/CARTEZOO.GIF";
        String tempTif = service.TMP_FILES_DIR + "/CARTEZOO.GIF.tif";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\tICCProfileName:null\t" +
                "ColorSpace:null\tInteropIndex:null\tPhotometricInterpretation:RGB Palette\t" +
                "MagickIdentify:\"Dimensions: 295x353;Channels: srgb;Bit-depth: 8;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: ;ICC Profile: ;ICM Profile: ;\"";

        service.convertImageFormats(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testConvertPictToTiff() throws Exception {
        String testFile = "src/test/resources/IMG_3444.pct";
        String tempTif = service.TMP_FILES_DIR + "/IMG_3444.pct.tif";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\tICCProfileName:sRGB IEC61966-2.1\t" +
                "ColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 1600x1200;Channels: srgb;Bit-depth: 8;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: 8bim,icc;ICC Profile: sRGB IEC61966-2.1;ICM Profile: ;\"";

        service.convertImageFormats(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testConvertBmpToTiff() throws Exception {
        String testFile = "src/test/resources/Wagoner_BW.bmp";
        String tempTif = service.TMP_FILES_DIR + "/Wagoner_BW.bmp.tif";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\tICCProfileName:null\t" +
                "ColorSpace:null\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 1940x2676;Channels: srgb;Bit-depth: 8;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: ;ICC Profile: ;ICM Profile: ;\"";

        service.convertImageFormats(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testTmpImageFileDirDeleted() throws Exception {
        String testFile = "src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif";
        service.convertCmykColorSpace(testFile);
        service.deleteTmpImageFilesDir();

        assertFalse(Files.exists(service.TMP_FILES_DIR));
    }

}
