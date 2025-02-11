package JP2ImageConverter.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

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
        colorFieldsService = new ColorFieldsService();
        imagePreproccessingService = new ImagePreproccessingService();
        imagePreproccessingService.tmpFilesDir = tmpFolder;
        service = new KakaduService();
        service.setColorFieldsService(colorFieldsService);
        service.setImagePreproccessingService(imagePreproccessingService);
    }

    @Test
    public void testRetrieveColorInfo() throws Exception {
        // EXIF ColorSpace is null, EXIF PhotometricInterpretation is gray
        String testFile = "src/test/resources/P0024_0066.tif";
        var originalImageMetadata = service.extractMetadata(testFile, "tiff");
        var info = service.getColorInfo(originalImageMetadata, originalImageMetadata, testFile);
        assertEquals("Gray", info.get(KakaduService.COLOR_SPACE));
        assertEquals("Grayscale", info.get(KakaduService.COLOR_TYPE));
    }

    @Test
    public void testKduCompressTiff() throws Exception {
        String testFile = "src/test/resources/E101_F8_0112.tif";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/E101_F8_0112"), "");

        assertTrue(Files.exists(tmpFolder.resolve("E101_F8_0112.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressGrayscaleTiff() throws Exception {
        String testFile = "src/test/resources/P0024_0103_01.tif";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/P0024_0103_01"), "");

        assertTrue(Files.exists(tmpFolder.resolve("P0024_0103_01.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressCmykTiff() throws Exception {
        String testFile = "src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/OP20459_1_TremorsKelleyandtheCowboys"),
                "");

        assertTrue(Files.exists(tmpFolder.resolve("OP20459_1_TremorsKelleyandtheCowboys.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressJpeg() throws Exception {
        String testFile = "src/test/resources/IMG_2377.jpeg";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/IMG_2377"), "");

        assertTrue(Files.exists(tmpFolder.resolve("IMG_2377.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressColorFilterArrayJpeg() throws Exception {
        String testFile = "src/test/resources/DSC_0052.jpeg";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/DSC_0052"), "");

        assertTrue(Files.exists(tmpFolder.resolve("DSC_0052.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressPng() throws Exception {
        String testFile = "src/test/resources/schoolphotos1.png";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/schoolphotos1"), "");

        assertTrue(Files.exists(tmpFolder.resolve("schoolphotos1.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressGif() throws Exception {
        String testFile = "src/test/resources/CARTEZOO.GIF";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/CARTEZOO"), "");

        assertTrue(Files.exists(tmpFolder.resolve("CARTEZOO.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressPict() throws Exception {
        String testFile = "src/test/resources/IMG_3444.pct";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/IMG_3444"), "");

        assertTrue(Files.exists(tmpFolder.resolve("IMG_3444.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressBmp() throws Exception {
        String testFile = "src/test/resources/Wagoner_BW.bmp";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/Wagoner_BW"), "");

        assertTrue(Files.exists(tmpFolder.resolve("Wagoner_BW.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressPsd() throws Exception {
        String testFile = "src/test/resources/17.psd";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/17"), "");

        assertTrue(Files.exists(tmpFolder.resolve("17.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressJp2() throws Exception {
        String testFile = "src/test/resources/17.jp2";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/17"), "");

        assertTrue(Files.exists(tmpFolder.resolve("17.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressTifWithInvalidIccProfile() throws Exception {
        String testFile = "src/test/resources/invalid_icc_profile.tif";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/invalid_icc_profile"), "");

        assertTrue(Files.exists(tmpFolder.resolve("invalid_icc_profile.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressTifWithSrgbAndTypeGray() throws Exception {
        String testFile = "src/test/resources/obama_smoking.tiff";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/obama_smoking"), "");

        assertTrue(Files.exists(tmpFolder.resolve("obama_smoking.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressNef() throws Exception {
        String testFile = "src/test/resources/20170822_068.NEF";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/20170822_068"), "");

        assertTrue(Files.exists(tmpFolder.resolve("20170822_068.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressNrw() throws Exception {
        String testFile = "src/test/resources/20170726_010.NRW";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/20170726_010"), "");

        assertTrue(Files.exists(tmpFolder.resolve("20170726_010.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressCrw() throws Exception {
        String testFile = "src/test/resources/CanonEOS10D.crw";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/CanonEOS10D"), "");

        assertTrue(Files.exists(tmpFolder.resolve("CanonEOS10D.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressCr2() throws Exception {
        String testFile = "src/test/resources/CanonEOS350D.CR2";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/CanonEOS350D"), "");

        assertTrue(Files.exists(tmpFolder.resolve("CanonEOS350D.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressDng() throws Exception {
        String testFile = "src/test/resources/DJIPhantom4.dng";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/DJIPhantom4"), "");

        assertTrue(Files.exists(tmpFolder.resolve("DJIPhantom4.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressRaf() throws Exception {
        String testFile = "src/test/resources/FujiFilmFinePixS5500.raf";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/FujiFilmFinePixS5500"), "");

        assertTrue(Files.exists(tmpFolder.resolve("FujiFilmFinePixS5500.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testKduCompressFail() throws Exception {
        String testFile = "src/test/resources/test_input.txt";

        try {
            service.kduCompress(testFile, Paths.get(tmpFolder + "/test_input"), "");
            fail();
        } catch (Exception e) {
            assertContains("JP2 conversion for the following file format not supported: txt", e.getMessage());
        }
    }

    @Test
    public void testNonExistentOutputPathFail() throws Exception {
        String testFile = "src/test/resources/E101_F8_0112.tif";

        try {
            service.kduCompress(testFile, Paths.get("folder/E101_F8_0112"), "");
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("folder/E101_F8_0112 does not exist."));
            assertFalse(Files.exists(Paths.get("folder/E101_F8_0112.jp2")));
        }
    }

    @Test
    public void testSourceFormatJpegWithTiffExtension() throws Exception {
        String testFile = "src/test/resources/IMG_2377_sfjpeg.tif";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/IMG_2377_sfjpeg"), "jpeg");

        assertTrue(Files.exists(tmpFolder.resolve("IMG_2377_sfjpeg.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testSourceFormatJpegWithNoFileExtension() throws Exception {
        String testFile = "src/test/resources/IMG_2377_nofileext";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/IMG_2377_nofileext"), "jpeg");

        assertTrue(Files.exists(tmpFolder.resolve("IMG_2377_nofileext.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
    }

    @Test
    public void testNoSourceFormatWithNoFileExtensionFail() throws Exception {
        String testFile = "src/test/resources/IMG_2377_nofileext";
        try {
            service.kduCompress(testFile, Paths.get(tmpFolder + "/IMG_2377_nofileext"), "");
            fail();
        } catch (Exception e) {
            assertContains("Source format could not be determined for", e.getMessage());
        }
    }

    @Test
    public void testUnrecognizedSourceFormatFail() throws Exception {
        String testFile = "src/test/resources/E101_F8_0112.tif";

        try {
            service.kduCompress(testFile, Paths.get(tmpFolder + "/E101_F8_0112"), "test");
            fail();
        } catch (Exception e) {
            assertContains("JP2 conversion for the following file format not supported: test", e.getMessage());
        }
    }

    @Test
    public void testListOfFilesKduCompress() throws Exception {
        String testFile = "src/test/resources/test_input.txt";
        service.fileListKduCompress(testFile, tmpFolder, "");

        assertTrue(Files.exists(tmpFolder.resolve("E101_F8_0112.jp2")));
        assertTrue(Files.exists(tmpFolder.resolve("P0024_0066.jp2")));
        assertEquals(2, Files.list(tmpFolder).count());
    }

    @Test
    public void testListofFilesWithNonexistentFileKduCompress() throws Exception {
        String testFile = "src/test/resources/test_input_fail.txt";

        try {
            service.fileListKduCompress(testFile, tmpFolder, "");
            fail();
        } catch (Exception e) {
            assertContains("src/test/resources/test.tif does not exist. Not processing file list further.",
                    e.getMessage());
        }
    }

    @Test
    public void testDeleteTinyGrayVoidImage() throws Exception {
        String testFile = "src/test/resources/04OldWelllogo.psd";
        service.kduCompress(testFile, Paths.get(tmpFolder + "/04OldWelllogo"), "psd");

        assertEquals(0, Files.list(tmpFolder).count());
    }

    @Test
    public void testCreateLinkToOriginal() throws Exception {
        String testFile = "src/test/resources/IMG_2377_nofileext";

        var intermediateFiles = new ArrayList<String>();
        String result = service.linkToOriginal(testFile, "jpeg", intermediateFiles);

        assertTrue(result.contains(service.tmpDir.toString()));
        assertTrue(result.contains("/IMG_2377_nofileext"));
        assertTrue(result.endsWith(".jpeg"));
        assertTrue(intermediateFiles.contains(result));
    }

    @Test
    public void testCreateLinkToOriginalSourceFormatMatchesExtension() throws Exception {
        String testFile = "src/test/resources/IMG_2377.jpeg";

        var intermediateFiles = new ArrayList<String>();
        String result = service.linkToOriginal(testFile, "jpeg", intermediateFiles);

        assertEquals(testFile, result);
        assertTrue(intermediateFiles.isEmpty());
    }

    @Test
    public void testKduCompressRotatedTiff() throws Exception {
        System.setOut(new PrintStream(outputStreamCaptor));
        String testFile = "src/test/resources/rotated.tiff";
        colorFieldsService.listFields(testFile);
        String output = outputStreamCaptor.toString();
        assertContains("2574x3083", output);

        service.kduCompress(testFile, Paths.get(tmpFolder + "/rotated"), "");

        assertTrue(Files.exists(tmpFolder.resolve("rotated.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
        var errorOriginal = System.err;
        try {
            System.setErr(new PrintStream(outputStreamCaptor));
            colorFieldsService.listFields(tmpFolder.resolve("rotated.jp2").toString());
        } finally {
            System.setErr(errorOriginal);
        }
        output = outputStreamCaptor.toString();
        // Dimensions after rotation
        assertContains("3083x2574", output);
    }

    @Test
    public void testKduCompressRotatedJpg() throws Exception {
        System.setOut(new PrintStream(outputStreamCaptor));
        String testFile = "src/test/resources/albright_football_0082.jpg";
        colorFieldsService.listFields(testFile);
        String output = outputStreamCaptor.toString();
        assertContains("4256x2832", output);

        try {
            service.kduCompress(testFile, Paths.get(tmpFolder + "/albright_football_0082"), "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(Files.exists(tmpFolder.resolve("albright_football_0082.jp2")));
        assertEquals(1, Files.list(tmpFolder).count());
        var errorOriginal = System.err;
        try {
            System.setErr(new PrintStream(outputStreamCaptor));
            colorFieldsService.listFields(tmpFolder.resolve("albright_football_0082.jp2").toString());
        } finally {
            System.setErr(errorOriginal);
        }
        output = outputStreamCaptor.toString();
        // Dimensions after rotation
        assertContains("2832x4256", output);
    }

    @Test
    public void testKduCompressTiffWithPaletteTypeAndColorSpace() throws Exception {
        String mockedTif = String.valueOf(new File( tmpFolder + "/mockedImage.tif"));
        Map<String, String> imageMetadata = Map.of(ColorFieldsService.COLOR_SPACE, "RGB Palette");
        ColorFieldsService colorFieldsService = mock(ColorFieldsService.class);
        when(colorFieldsService.extractMetadataFields(anyString())).thenReturn(imageMetadata);
        when(colorFieldsService.identifyType(anyString())).thenReturn("Palette");
        ImagePreproccessingService imagePreproccessingService = mock(ImagePreproccessingService.class);
        when(imagePreproccessingService.convertToTiff(anyString(), anyString())).thenReturn(mockedTif);
        when(imagePreproccessingService.convertColorSpaces(anyString(), anyString())).thenReturn(mockedTif);

        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            File mockedJp2 = new File(tmpFolder + "/mockedImage.jp2");
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList())).thenReturn(String.valueOf(mockedJp2));

            KakaduService service = new KakaduService();
            service.setColorFieldsService(colorFieldsService);
            service.setImagePreproccessingService(imagePreproccessingService);
            service.kduCompress(mockedTif, tmpFolder.resolve("mockedImage"), "");

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("kdu_compress", "-i", mockedTif, "-o", mockedJp2.toString(),
                            "Clevels=6", "Clayers=6", "Cprecincts={256,256},{256,256},{128,128}", "" +
                            "Stiles={512,512}", "Corder=RPCL", "ORGgen_plt=yes", "ORGtparts=R", "Cblk={64,64}",
                            "Cuse_sop=yes", "Cuse_eph=yes", "-flush_period", "1024", "-rate", "3", "-no_weights"))));
            verify(imagePreproccessingService, times(1)).convertToTiff(mockedTif, "tif");
            verify(imagePreproccessingService, times(1)).convertColorSpaces("RGB Palette", "Palette", mockedTif);
        }
    }

    private void assertContains(String expected, String actual) {
        assertTrue(actual.contains(expected), "Expected string '" + expected + "' not found: " + actual);
    }
}
