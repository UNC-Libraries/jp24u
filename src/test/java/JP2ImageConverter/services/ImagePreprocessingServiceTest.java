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
        var tempTif = service.convertCmykAndYcbcrColorSpace(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains("RGB"));
        assertFalse(outputStreamCaptor.toString().contains("CMYK"));
    }

    @Disabled("testFile is 155.6MB, too big to add to github")
    @Test
    public void testConvertCmykImageWithoutIccProfile() throws Exception {
        String testFile = "src/test/resources/Surgery.tif";
        var tempTif = service.convertCmykAndYcbcrColorSpace(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains("RGB"));
        assertFalse(outputStreamCaptor.toString().contains("CMYK"));
    }

    @Test
    public void testConvertJpegtoPpm() throws Exception {
        String testFile = "src/test/resources/IMG_2377.jpeg";

        var tempPpm = service.convertJpeg(testFile);

        assertTrue(Files.exists(Paths.get(tempPpm)));
    }

    @Test
    public void testConvertPngToTiff() throws Exception {
        String testFile = "src/test/resources/schoolphotos1.png";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\tICCProfileName:Adobe RGB (1998)\t" +
                "ColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 1300x2000;Channels: srgb;Bit-depth: 16;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: icc;ICC Profile: Adobe RGB (1998);ICM Profile: ;Type: TrueColor;\"";

        var tempTif = service.convertImageFormats(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testConvertGifToTiff() throws Exception {
        String testFile = "src/test/resources/CARTEZOO.GIF";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\tICCProfileName:null\t" +
                "ColorSpace:null\tInteropIndex:null\tPhotometricInterpretation:RGB Palette\t" +
                "MagickIdentify:\"Dimensions: 295x353;Channels: srgb;Bit-depth: 8;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: ;ICC Profile: ;ICM Profile: ;Type: Grayscale;\"";

        var tempTif = service.convertImageFormats(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testConvertPictToTiff() throws Exception {
        String testFile = "src/test/resources/IMG_3444.pct";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\tICCProfileName:sRGB IEC61966-2.1\t" +
                "ColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 1600x1200;Channels: srgb;Bit-depth: 8;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: 8bim,icc;ICC Profile: sRGB IEC61966-2.1;ICM Profile: ;Type: TrueColor;\"";

        var tempTif = service.convertImageFormats(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testConvertBmpToTiff() throws Exception {
        String testFile = "src/test/resources/Wagoner_BW.bmp";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\tICCProfileName:null\t" +
                "ColorSpace:null\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 1940x2676;Channels: srgb;Bit-depth: 8;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: ;ICC Profile: ;ICM Profile: ;Type: TrueColor;\"";

        var tempTif = service.convertImageFormats(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testConvertPsdToTiff() throws Exception {
        String testFile = "src/test/resources/17.psd";
        String tifExifData = "DateTimeOriginal:2008:02:07 13:05:19\tDateTimeDigitized:2008:02:07 13:05:19\t" +
                "ICCProfileName:sRGB IEC61966-2.1\tColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 1228x1818;Channels: srgb;Bit-depth: 8;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: 8bim,exif,icc,iptc,xmp;ICC Profile: sRGB IEC61966-2.1;" +
                "ICM Profile: ;Type: TrueColor;\"";

        var tempTif = service.convertPsd(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testConvertJp2toTiff() throws Exception {
        String testFile = "src/test/resources/17.jp2";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\t" +
                "ICCProfileName:sRGB IEC61966-2.1\tColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 1228x1818;Channels: srgb;Bit-depth: 8;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: icc;ICC Profile: sRGB IEC61966-2.1;ICM Profile: ;Type: TrueColor;\"";

        var tempTif = service.convertJp2(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testConvertNefToJpeg() throws Exception {
        String testFile = "src/test/resources/20170822_068.NEF";
        String tempTif = service.tmpFilesDir + "/20170822_068.NEF.jpeg";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\t" +
                "ICCProfileName:null\tColorSpace:null\tInteropIndex:null\tPhotometricInterpretation:null\t" +
                "MagickIdentify:\"Dimensions: 4272x2848;Channels: srgb;Bit-depth: 8;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: ;ICC Profile: ;ICM Profile: ;Type: TrueColor;\"";

        service.convertNef(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testConvertCrwToTiff() throws Exception {
        String testFile = "src/test/resources/CanonEOS10D.crw";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\t" +
                "ICCProfileName:sRGB\tColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 2056x3088;Channels: srgb;Bit-depth: 16;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: icc;ICC Profile: sRGB;ICM Profile: ;Type: TrueColor;\"";

        var tempTif = service.convertImageFormats(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testConvertCr2ToTiff() throws Exception {
        String testFile = "src/test/resources/CanonEOS350D.CR2";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\t" +
                "ICCProfileName:null\tColorSpace:null\tInteropIndex:null\tPhotometricInterpretation:null\t" +
                "MagickIdentify:\"Dimensions: 3474x2314;Channels: srgb;Bit-depth: 16;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: ;ICC Profile: ;ICM Profile: ;Type: TrueColor;\"";

        var tempPpm = service.convertCr2(testFile);
        colorFieldsService.listFields(tempPpm);

        assertTrue(Files.exists(Paths.get(tempPpm)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testConvertDngToTiff() throws Exception {
        String testFile = "src/test/resources/DJIPhantom4.dng";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\t" +
                "ICCProfileName:sRGB\tColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 4000x3000;Channels: srgb;Bit-depth: 16;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: icc;ICC Profile: sRGB;ICM Profile: ;Type: TrueColor;\"";

        var tempTif = service.convertImageFormats(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testConvertRafToTiff() throws Exception {
        String testFile = "src/test/resources/FujiFilmFinePixS5500.raf";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\t" +
                "ICCProfileName:sRGB\tColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 2304x1740;Channels: srgb;Bit-depth: 16;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: icc;ICC Profile: sRGB;ICM Profile: ;Type: TrueColor;\"";

        var tempTif = service.convertImageFormats(testFile);
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
    }

    @Test
    public void testImagePreprocessingPict() throws Exception {
        String testFile = "src/test/resources/IMG_3444.pct";
        String tifExifData = "DateTimeOriginal:null\tDateTimeDigitized:null\tICCProfileName:sRGB IEC61966-2.1\t" +
                "ColorSpace:RGB\tInteropIndex:null\tPhotometricInterpretation:RGB\t" +
                "MagickIdentify:\"Dimensions: 1600x1200;Channels: srgb;Bit-depth: 8;Alpha channel: False;" +
                "Color Space: sRGB;Profiles: 8bim,icc;ICC Profile: sRGB IEC61966-2.1;ICM Profile: ;Type: TrueColor;\"";

        var tempTif = service.convertToTiff(testFile, "");
        colorFieldsService.listFields(tempTif);

        assertTrue(Files.exists(Paths.get(tempTif)));
        assertTrue(tempTif.matches(".*/IMG_3444\\.pct.*\\.tif"));
        assertTrue(outputStreamCaptor.toString().contains(tifExifData));
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
