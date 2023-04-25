package colorscanner.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImagePreprocessingServiceTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private ColorFieldsService colorFieldsService;
    private ImagePreproccessingService service;

    @BeforeEach
    public void setup() throws Exception {
        System.setOut(new PrintStream(outputStreamCaptor));

        colorFieldsService = new ColorFieldsService();
        service = new ImagePreproccessingService();
    }

    @Test
    public void testConvertCmykImageWithIccProfile() throws Exception {
        String testFile = "src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif";
        service.convertCmykColorSpace(testFile);
        colorFieldsService.listFields(service.TMP_FILES_DIR +
                "/OP20459_1_TremorsKelleyandtheCowboys.tif.tif");

        assertTrue(Files.exists(Paths.get(service.TMP_FILES_DIR +
                "/OP20459_1_TremorsKelleyandtheCowboys.tif.tif")));
        assertTrue(outputStreamCaptor.toString().contains("RGB"));
        assertFalse(outputStreamCaptor.toString().contains("CMYK"));
    }

    @Disabled("testFile is 155.6MB, too big to add to github")
    @Test
    public void testConvertCmykImageWithoutIccProfile() throws Exception {
        String testFile = "src/test/resources/Surgery.tif";
        service.convertCmykColorSpace(testFile);
        colorFieldsService.listFields(service.TMP_FILES_DIR + "/Surgery.tif.tif");

        assertTrue(Files.exists(Paths.get(service.TMP_FILES_DIR + "/Surgery.tif.tif")));
        assertTrue(outputStreamCaptor.toString().contains("RGB"));
        assertFalse(outputStreamCaptor.toString().contains("CMYK"));
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
    public void testImagePreprocessingPict() throws Exception {
        String testFile = "src/test/resources/IMG_3444.pct";
        String tempTif = service.TMP_FILES_DIR + "/IMG_3444.pct.tif";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\tICCProfileName:sRGB IEC61966-2.1\t" +
                "ColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 1600x1200;Channels: srgb;Bit-depth: 8;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: 8bim,icc;ICC Profile: sRGB IEC61966-2.1;ICM Profile: ;\"";

        service.convertToTiff(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testImagePreprocessingTiff() throws Exception {
        String testFile = "src/test/resources/E101_F8_0112.tif";
        String tempTif = service.TMP_FILES_DIR + "/E101_F8_0112.tif.tif";

        service.convertToTiff(testFile);

        assertFalse(Files.exists(Paths.get(tempTif)));
    }

    @Test
    public void testConvertColorspace() throws Exception {
        String testFile = "src/test/resources/IMG_3444.pct.tif";

        String result = service.convertColorSpaces("rgb", testFile);

        assertEquals(testFile, result);
    }

    @Test
    public void testConvertUnusualColorspace() throws Exception {
        String testFile = "src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif";

        service.convertColorSpaces("cmyk", testFile);

        assertTrue(Files.exists(Paths.get(service.TMP_FILES_DIR +
                "/OP20459_1_TremorsKelleyandtheCowboys.tif.tif")));
    }

    @Test
    public void testTmpImageFileDirDeleted() throws Exception {
        String testFile = "src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif";
        service.convertCmykColorSpace(testFile);
        service.deleteTmpImageFilesDir();

        assertFalse(Files.exists(service.TMP_FILES_DIR));
    }

}
