package JP2ImageConverter.services;

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

public class ImagePreprocessingServiceTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @TempDir
    public Path tmpFolder;

    private ColorFieldsService colorFieldsService;
    private ImagePreproccessingService service;

    @BeforeEach
    public void setup() throws Exception {
        System.setOut(new PrintStream(outputStreamCaptor));

        colorFieldsService = new ColorFieldsService();
        service = new ImagePreproccessingService();
        service.tmpFilesDir = tmpFolder;
    }

    @Test
    public void testConvertCmykImageWithIccProfile() throws Exception {
        String testFile = "src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif";
        var tempTif = service.setColorSpaceRemoveProfileWithIm(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains("RGB"));
        assertFalse(outputStreamCaptor.toString().contains("CMYK"));
    }

    @Disabled("testFile is 155.6MB, too big to add to github")
    @Test
    public void testConvertCmykImageWithoutIccProfile() throws Exception {
        String testFile = "src/test/resources/Surgery.tif";
        var tempTif = service.setColorSpaceRemoveProfileWithIm(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains("RGB"));
        assertFalse(outputStreamCaptor.toString().contains("CMYK"));
    }

    @Test
    public void testConvertJpegtoPpm() throws Exception {
        String testFile = "src/test/resources/IMG_2377.jpeg";

        var tempPpm = service.convertToPpmWithIm(testFile);

        assertTrue(Files.exists(Paths.get(tempPpm)));
    }

    @Test
    public void testConvertRw2ToPpm() throws Exception {
        String testFile = "src/test/resources/test.RW2";

        var tempPpm = service.convertToPpmWithDcraw(testFile);

        assertTrue(Files.exists(Paths.get(tempPpm)));
    }

    @Test
    public void testConvertPngToTiff() throws Exception {
        String testFile = "src/test/resources/schoolphotos1.png";

        var tempTif = service.convertToTifWithGm(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        var attributes = outputStreamCaptor.toString();
        assertContains("ICCProfileName:Adobe RGB", attributes);
        assertContains("MagickIdentify:", attributes);
        assertContains("Dimensions: 1300x2000;", attributes);
        assertContains("Channels: srgb", attributes);
        assertContains("Bit-depth: 16;", attributes);
        assertContains("Color Space: sRGB;", attributes);
        assertContains("Profiles: icc;", attributes);
        assertContains("Type: TrueColor;", attributes);
    }

    @Test
    public void testConvertGifToTiff() throws Exception {
        String testFile = "src/test/resources/CARTEZOO.GIF";

        var tempTif = service.convertToTifWithGm(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        var attributes = outputStreamCaptor.toString();
        assertContains("MagickIdentify:", attributes);
        assertContains("Dimensions: 295x353;", attributes);
        assertContains("Channels: srgb", attributes);
        assertContains("Bit-depth: 8;", attributes);
        assertContains("Color Space: sRGB;", attributes);
        assertContains("Type: Grayscale;", attributes);
    }

    @Test
    public void testConvertPictToTiff() throws Exception {
        String testFile = "src/test/resources/IMG_3444.pct";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\tICCProfileName:sRGB IEC61966-2.1\t" +
                "ColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 1600x1200;Channels: srgb  3.0;Bit-depth: 8;Alpha channel: Undefined;" +
                "Color Space: sRGB;Profiles: 8bim,icc;ICC Profile: sRGB IEC61966-2.1;ICM Profile: ;Type: TrueColor;\"";

        var tempTif = service.convertToTifWithGm(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        var attributes = outputStreamCaptor.toString();
        assertContains("MagickIdentify:", attributes);
        assertContains("Dimensions: 1600x1200;", attributes);
        assertContains("Channels: srgb", attributes);
        assertContains("Bit-depth: 8;", attributes);
        assertContains("Color Space: sRGB;", attributes);
        assertContains("Profiles: 8bim,icc;", attributes);
        assertContains("ICC Profile: sRGB IEC61966-2.1", attributes);
        assertContains("Type: TrueColor;", attributes);
    }

    @Test
    public void testConvertBmpToTiff() throws Exception {
        String testFile = "src/test/resources/Wagoner_BW.bmp";

        var tempTif = service.convertToTifWithGm(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        var attributes = outputStreamCaptor.toString();
        assertContains("MagickIdentify:", attributes);
        assertContains("Dimensions: 1940x2676;", attributes);
        assertContains("Channels: srgb", attributes);
        assertContains("Color Space: sRGB;", attributes);
        assertContains("Type: TrueColor;", attributes);
    }

    private void assertContains(String expected, String actual) {
        assertTrue(actual.contains(expected), "Expected string '" + expected + "' not found: " + actual);
    }

    @Test
    public void testConvertPsdToTiff() throws Exception {
        String testFile = "src/test/resources/17.psd";

        var tempTif = service.flattenSetColorspaceConvertToTifWithIm(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        var attributes = outputStreamCaptor.toString();
        assertContains("MagickIdentify:", attributes);
        assertContains("Dimensions: 1228x1818;", attributes);
        assertContains("Channels: srgb", attributes);
        assertContains("Color Space: sRGB;", attributes);
        assertContains("Profiles: 8bim,exif,icc,iptc,xmp;", attributes);
        assertContains("Type: TrueColor;", attributes);
    }

    @Test
    public void testConvertJp2toTiff() throws Exception {
        String testFile = "src/test/resources/17.jp2";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\t" +
                "ICCProfileName:sRGB IEC61966-2.1\tColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 1228x1818;Channels: srgb  3.0;Bit-depth: 8;Alpha channel: Undefined;" +
                "Color Space: sRGB;Profiles: icc;ICC Profile: sRGB IEC61966-2.1;ICM Profile: ;Type: TrueColor;\"";

        var tempTif = service.convertToTifWithIm(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        var attributes = outputStreamCaptor.toString();
        assertContains("ICCProfileName:sRGB IEC61966-2.1", attributes);
        assertContains("MagickIdentify:", attributes);
        assertContains("Dimensions: 1228x1818;", attributes);
        assertContains("Channels: srgb", attributes);
        assertContains("Color Space: sRGB;", attributes);
        assertContains("Profiles: icc;", attributes);
        assertContains("Type: TrueColor;", attributes);
    }

    @Test
    public void testConvertNefToJpeg() throws Exception {
        String testFile = "src/test/resources/20170822_068.NEF";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\t" +
                "ICCProfileName:null\tColorSpace:null\tInteropIndex:null\tPhotometricInterpretation:null\t" +
                "MagickIdentify:\"Dimensions: 4272x2848;Channels: srgb  3.0;Bit-depth: 8;Alpha channel: Undefined;" +
                "Color Space: sRGB;Profiles: ;ICC Profile: ;ICM Profile: ;Type: TrueColor;\"";

        var tempTif = service.convertToJpgWithExiftool(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        var attributes = outputStreamCaptor.toString();
        assertContains("MagickIdentify:", attributes);
        assertContains("Dimensions: 4272x2848;", attributes);
        assertContains("Channels: srgb", attributes);
        assertContains("Color Space: sRGB;", attributes);
        assertContains("Profiles: exif;", attributes);
        assertContains("Type: TrueColor;", attributes);
    }

    @Test
    public void testConvertNrwToJpeg() throws Exception {
        String testFile = "src/test/resources/20170726_010.NRW";

        var tempJpeg = service.convertToJpgWithExiftool(testFile);
        colorFieldsService.listFields(tempJpeg);

        assertTrue(Files.exists(Paths.get(tempJpeg)));
        var attributes = outputStreamCaptor.toString();
        assertContains("MagickIdentify:", attributes);
        assertContains("Dimensions: 4000x3000;", attributes);
        assertContains("Channels: srgb  3.0", attributes);
        assertContains("Color Space: sRGB;", attributes);
        assertContains("Profiles: exif;", attributes);
        assertContains("Type: TrueColor;", attributes);
    }

    @Test
    public void testConvertCrwToTiff() throws Exception {
        String testFile = "src/test/resources/CanonEOS10D.crw";

        var tempTif = service.convertToTifWithGm(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        var attributes = outputStreamCaptor.toString();
        assertContains("MagickIdentify:", attributes);
        assertContains("Dimensions: 2056x3088;", attributes);
        assertContains("Channels: srgb", attributes);
        assertContains("Bit-depth: 16;", attributes);
        assertContains("Color Space: sRGB;", attributes);
        assertContains("Profiles: icc;", attributes);
        assertContains("Type: TrueColor;", attributes);
    }

    @Test
    public void testConvertCr2ToTiff() throws Exception {
        String testFile = "src/test/resources/CanonEOS350D.CR2";

        var tempPpm = service.convertToPpmWithGm(testFile);

        assertTrue(Files.exists(Paths.get(tempPpm)));
    }

    @Test
    public void testConvertPcdToTiff() throws Exception {
        String testFile = "src/test/resources/98-337.03.PCD";

        var tempTif = service.convertToTifHighestResolutionWithGm(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        var attributes = outputStreamCaptor.toString();
        assertContains("MagickIdentify:", attributes);
        assertContains("Dimensions: 4096x6144;", attributes);
        assertContains("Channels: srgb", attributes);
        assertContains("Color Space: sRGB;", attributes);
        assertContains("Type: TrueColor;", attributes);
    }

    @Test
    public void testConvertDngToTiff() throws Exception {
        String testFile = "src/test/resources/DJIPhantom4.dng";

        var tempTif = service.convertToTifWithGm(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        var attributes = outputStreamCaptor.toString();
        assertContains("MagickIdentify:", attributes);
        assertContains("Dimensions: 4000x3000;", attributes);
        assertContains("Channels: srgb", attributes);
        assertContains("Bit-depth: 16;", attributes);
        assertContains("Color Space: sRGB;", attributes);
        assertContains("Profiles: icc;", attributes);
        assertContains("Type: TrueColor;", attributes);
    }

    @Test
    public void testConvertRafToTiff() throws Exception {
        String testFile = "src/test/resources/FujiFilmFinePixS5500.raf";

        var tempTif = service.convertToTifWithGm(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        var attributes = outputStreamCaptor.toString();
        assertContains("MagickIdentify:", attributes);
        assertContains("Dimensions: 2304x1740;", attributes);
        assertContains("Channels: srgb", attributes);
        assertContains("Color Space: sRGB;", attributes);
        assertContains("Profiles: icc;", attributes);
        assertContains("Type: TrueColor;", attributes);
    }

    @Test
    public void testImagePreprocessingPict() throws Exception {
        String testFile = "src/test/resources/IMG_3444.pct";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\tICCProfileName:sRGB IEC61966-2.1\t" +
                "ColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 1600x1200;Channels: srgb  3.0;Bit-depth: 8;Alpha channel: Undefined;" +
                "Color Space: sRGB;Profiles: 8bim,icc;ICC Profile: sRGB IEC61966-2.1;ICM Profile: ;Type: TrueColor;\"";

        var tempTif = service.convertToTiff(testFile, "");
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(tempTif.matches(".*/IMG_3444\\.pct.*\\.tif"));
        var attributes = outputStreamCaptor.toString();
        assertContains("MagickIdentify:", attributes);
        assertContains("Dimensions: 1600x1200;", attributes);
        assertContains("Channels: srgb", attributes);
        assertContains("Bit-depth: 8;", attributes);
        assertContains("Color Space: sRGB;", attributes);
        assertContains("Profiles: 8bim,icc;", attributes);
        assertContains("Type: TrueColor;", attributes);
    }

    @Test
    public void testImagePreprocessingTiff() throws Exception {
        String testFile = "src/test/resources/E101_F8_0112.tif";

        var tempTif = service.convertToTiff(testFile, "");

        assertTrue(Files.exists(Paths.get(tempTif)));
    }

    @Test
    public void testImagePreprocessingNef() throws Exception {
        String testFile = "src/test/resources/20170822_068.NEF";

        var tempTif = service.convertToTiff(testFile, "");
        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(tempTif.matches(".*/20170822_068\\.NEF.*\\.ppm"));
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

        String result = service.convertColorSpaces("cmyk", testFile);

        assertTrue(Files.exists(Paths.get(result)));
    }

    @Test
    public void testCreateLinkToTiff() throws Exception {
        String testFile = "src/test/resources/E101_F8_0112.tif";

        String result = service.linkToTiff(testFile);

        assertTrue(result.contains(service.tmpFilesDir.toString()));
        assertTrue(result.contains("/E101_F8_0112.tif"));
        assertTrue(result.endsWith(".tif"));
    }
}
