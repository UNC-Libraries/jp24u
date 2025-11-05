package JP2ImageConverter.services;

import JP2ImageConverter.util.CommandUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class KakaduServiceTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @TempDir
    public Path tmpFolder;

    private AutoCloseable closeable;

    private KakaduService service;
    private ColorFieldsService colorFieldsService;
    private ImagePreproccessingService imagePreproccessingService;

    @BeforeEach
    public void setup() throws Exception {
        closeable = openMocks(this);

        colorFieldsService = new ColorFieldsService();
        imagePreproccessingService = new ImagePreproccessingService();
        imagePreproccessingService.tmpFilesDir = tmpFolder;
        service = new KakaduService();
        service.setColorFieldsService(colorFieldsService);
        service.setImagePreproccessingService(imagePreproccessingService);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
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
        String mockedTif = tmpFolder.resolve("mockedImage.tif").toString();
        Map<String, String> imageMetadata = Map.of(ColorFieldsService.COLOR_SPACE, "RGB");
        ColorFieldsService colorFieldsService = mock(ColorFieldsService.class);
        when(colorFieldsService.extractMetadataFields(anyString())).thenReturn(imageMetadata);
        when(colorFieldsService.identifyType(anyString())).thenReturn("TrueColor");
        ImagePreproccessingService imagePreproccessingService = mock(ImagePreproccessingService.class);
        when(imagePreproccessingService.convertToTiff(anyString(), anyString())).thenReturn(mockedTif);
        when(imagePreproccessingService.convertColorSpaces(anyString(), anyString(), anyString())).thenReturn(mockedTif);

        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String mockedJp2 = tmpFolder.resolve("mockedImage.jp2").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn("use -no_palette to avoid nasty palettization effects")
                    .thenReturn(mockedJp2);

            KakaduService service = new KakaduService();
            service.setColorFieldsService(colorFieldsService);
            service.setImagePreproccessingService(imagePreproccessingService);
            service.kduCompress(mockedTif, tmpFolder.resolve("mockedImage"), "");

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("kdu_compress", "-i", mockedTif, "-o", mockedJp2,
                            "Clevels=6", "Clayers=6",
                            "Cprecincts={256,256},{256,256},{128,128}", "Stiles={512,512}", "Corder=RPCL",
                            "ORGgen_plt=yes", "ORGtparts=R", "Cblk={64,64}", "Cuse_sop=yes", "Cuse_eph=yes",
                            "-flush_period", "1024", "-rate", "3", "-no_weights"))));
            verify(imagePreproccessingService, times(1))
                    .convertToTiff(mockedTif, "tiff");
            verify(imagePreproccessingService, times(1))
                    .convertColorSpaces("RGB", "TrueColor", mockedTif);
        }
    }

    @Test
    public void testKduCompressGrayColorspaceTiff() throws Exception {
        String mockedTif = tmpFolder.resolve("mockedImage.tif").toString();
        Map<String, String> imageMetadata = Map.of(ColorFieldsService.COLOR_SPACE, "Gray");
        ColorFieldsService colorFieldsService = mock(ColorFieldsService.class);
        when(colorFieldsService.extractMetadataFields(anyString())).thenReturn(imageMetadata);
        when(colorFieldsService.identifyType(anyString())).thenReturn("TrueColor");
        ImagePreproccessingService imagePreproccessingService = mock(ImagePreproccessingService.class);
        when(imagePreproccessingService.convertToTiff(anyString(), anyString())).thenReturn(mockedTif);
        when(imagePreproccessingService.convertColorSpaces(anyString(), anyString(), anyString())).thenReturn(mockedTif);

        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String mockedJp2 = tmpFolder.resolve("mockedImage.jp2").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(mockedJp2);

            KakaduService service = new KakaduService();
            service.setColorFieldsService(colorFieldsService);
            service.setImagePreproccessingService(imagePreproccessingService);
            service.kduCompress(mockedTif, tmpFolder.resolve("mockedImage"), "");

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("kdu_compress", "-i", mockedTif, "-o", mockedJp2,
                            "Clevels=6", "Clayers=6",
                            "Cprecincts={256,256},{256,256},{128,128}", "Stiles={512,512}", "Corder=RPCL",
                            "ORGgen_plt=yes", "ORGtparts=R", "Cblk={64,64}", "Cuse_sop=yes", "Cuse_eph=yes",
                            "-flush_period", "1024", "-rate", "3", "-no_weights", "-jp2_space", "sLUM"))));
            verify(imagePreproccessingService, times(1))
                    .convertToTiff(mockedTif, "tiff");
            verify(imagePreproccessingService, times(1))
                    .convertColorSpaces("Gray", "TrueColor", mockedTif);
        }
    }

    @Test
    public void testKduCompressNonTif() throws Exception {
        String mockedJpeg = tmpFolder.resolve("mockedImage.jpeg").toString();
        Map<String, String> imageMetadata = Map.of(ColorFieldsService.COLOR_SPACE, "RGB");
        ColorFieldsService colorFieldsService = mock(ColorFieldsService.class);
        when(colorFieldsService.extractMetadataFields(anyString())).thenReturn(imageMetadata);
        when(colorFieldsService.identifyType(anyString())).thenReturn("TrueColor");
        ImagePreproccessingService imagePreproccessingService = mock(ImagePreproccessingService.class);
        when(imagePreproccessingService.convertToTiff(anyString(), anyString())).thenReturn(mockedJpeg);
        when(imagePreproccessingService.convertColorSpaces(anyString(), anyString(), anyString())).thenReturn(mockedJpeg);

        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String mockedJp2 = tmpFolder.resolve("mockedImage.jp2").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(mockedJp2);

            KakaduService service = new KakaduService();
            service.setColorFieldsService(colorFieldsService);
            service.setImagePreproccessingService(imagePreproccessingService);
            service.kduCompress(mockedJpeg, tmpFolder.resolve("mockedImage"), "");

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("kdu_compress", "-i", mockedJpeg, "-o", mockedJp2,
                            "Clevels=6", "Clayers=6",
                            "Cprecincts={256,256},{256,256},{128,128}", "Stiles={512,512}", "Corder=RPCL",
                            "ORGgen_plt=yes", "ORGtparts=R", "Cblk={64,64}", "Cuse_sop=yes", "Cuse_eph=yes",
                            "-flush_period", "1024", "-rate", "3", "-no_weights"))));
            verify(imagePreproccessingService, times(1))
                    .convertToTiff(mockedJpeg, "jpeg");
            verify(imagePreproccessingService, times(1))
                    .convertColorSpaces("RGB", "TrueColor", mockedJpeg);
        }
    }

    @Test
    public void testKduCompressGif() throws Exception {
        String mockedGif = tmpFolder.resolve("mockedImage.gif").toString();
        Map<String, String> imageMetadata = Map.of(ColorFieldsService.COLOR_SPACE, "RGB");
        ColorFieldsService colorFieldsService = mock(ColorFieldsService.class);
        when(colorFieldsService.extractMetadataFields(anyString())).thenReturn(imageMetadata);
        when(colorFieldsService.identifyType(anyString())).thenReturn("TrueColor");
        ImagePreproccessingService imagePreproccessingService = mock(ImagePreproccessingService.class);
        when(imagePreproccessingService.convertToTiff(anyString(), anyString())).thenReturn(mockedGif);
        when(imagePreproccessingService.convertColorSpaces(anyString(), anyString(), anyString())).thenReturn(mockedGif);

        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String mockedJp2 = tmpFolder.resolve("mockedImage.jp2").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(mockedJp2);

            KakaduService service = new KakaduService();
            service.setColorFieldsService(colorFieldsService);
            service.setImagePreproccessingService(imagePreproccessingService);
            service.kduCompress(mockedGif, tmpFolder.resolve("mockedImage"), "");

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("kdu_compress", "-i", mockedGif, "-o", mockedJp2,
                            "Clevels=6", "Clayers=6",
                            "Cprecincts={256,256},{256,256},{128,128}", "Stiles={512,512}", "Corder=RPCL",
                            "ORGgen_plt=yes", "ORGtparts=R", "Cblk={64,64}", "Cuse_sop=yes", "Cuse_eph=yes",
                            "-flush_period", "1024", "-rate", "3", "-no_weights", "-no_palette"))));
            verify(imagePreproccessingService, times(1))
                    .convertToTiff(mockedGif, "gif");
            verify(imagePreproccessingService, times(1))
                    .convertColorSpaces("RGB", "TrueColor", mockedGif);
        }
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
    public void testSourceFormatImageWithNoFileExtension() throws Exception {
        String mockedImage = tmpFolder.resolve("mockedImage").toString();
        Map<String, String> imageMetadata = Map.of(ColorFieldsService.COLOR_SPACE, "RGB");
        ColorFieldsService colorFieldsService = mock(ColorFieldsService.class);
        when(colorFieldsService.extractMetadataFields(anyString())).thenReturn(imageMetadata);
        when(colorFieldsService.identifyType(anyString())).thenReturn("TrueColor");
        ImagePreproccessingService imagePreproccessingService = mock(ImagePreproccessingService.class);
        when(imagePreproccessingService.convertToTiff(anyString(), anyString())).thenReturn(mockedImage);
        when(imagePreproccessingService.convertColorSpaces(anyString(), anyString(), anyString())).thenReturn(mockedImage);

        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String mockedJp2 = tmpFolder.resolve("mockedImage.jp2").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(mockedJp2);

            KakaduService service = new KakaduService();
            service.setColorFieldsService(colorFieldsService);
            service.setImagePreproccessingService(imagePreproccessingService);
            service.kduCompress(mockedImage, tmpFolder.resolve("mockedImage"), "jpeg");

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("kdu_compress", "-i", mockedImage, "-o", mockedJp2,
                            "Clevels=6", "Clayers=6",
                            "Cprecincts={256,256},{256,256},{128,128}", "Stiles={512,512}", "Corder=RPCL",
                            "ORGgen_plt=yes", "ORGtparts=R", "Cblk={64,64}", "Cuse_sop=yes", "Cuse_eph=yes",
                            "-flush_period", "1024", "-rate", "3", "-no_weights"))));
            verify(imagePreproccessingService, times(1))
                    .convertColorSpaces("RGB", "TrueColor", mockedImage);
        }
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
        Map<String, String> imageMetadata = Map.of(ColorFieldsService.COLOR_SPACE, "Gray");
        ColorFieldsService colorFieldsService = mock(ColorFieldsService.class);
        when(colorFieldsService.extractMetadataFields(anyString())).thenReturn(imageMetadata);
        when(colorFieldsService.identifyType(anyString())).thenReturn("TrueColor");
        ImagePreproccessingService imagePreproccessingService = mock(ImagePreproccessingService.class);
        when(imagePreproccessingService.convertToTiff(anyString(), anyString())).thenReturn(testFile);
        when(imagePreproccessingService.convertColorSpaces(anyString(), anyString(), anyString())).thenReturn(testFile);

        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String mockedJp2 = tmpFolder.resolve("04OldWelllogo.jp2").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(mockedJp2);

            KakaduService service = new KakaduService();
            service.setColorFieldsService(colorFieldsService);
            service.setImagePreproccessingService(imagePreproccessingService);
            service.kduCompress(testFile, tmpFolder.resolve("04OldWelllogo"), "");

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("kdu_compress", "-i", testFile, "-o", mockedJp2,
                            "Clevels=6", "Clayers=6",
                            "Cprecincts={256,256},{256,256},{128,128}", "Stiles={512,512}", "Corder=RPCL",
                            "ORGgen_plt=yes", "ORGtparts=R", "Cblk={64,64}", "Cuse_sop=yes", "Cuse_eph=yes",
                            "-flush_period", "1024", "-rate", "3", "-no_weights", "-jp2_space", "sLUM"))));
            verify(imagePreproccessingService, times(1))
                    .convertToTiff(testFile, "psd");
            verify(imagePreproccessingService, times(1))
                    .convertColorSpaces("Gray", "TrueColor", testFile);
        }
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
    public void testKduCompressRotatedJpg() throws Exception {
        String testFile = "src/test/resources/albright_football_0082.jpg";
        Map<String, String> imageMetadata = Map.of(ColorFieldsService.COLOR_SPACE, "RGB",
                ColorFieldsService.ORIENTATION_DEFAULT, "Right side, top (Rotate 90 CW)");
        ColorFieldsService colorFieldsService = mock(ColorFieldsService.class);
        when(colorFieldsService.extractMetadataFields(anyString())).thenReturn(imageMetadata);
        when(colorFieldsService.identifyType(anyString())).thenReturn("TrueColor");
        when(colorFieldsService.identify(anyString())).thenReturn("4256x2832");
        ImagePreproccessingService imagePreproccessingService = mock(ImagePreproccessingService.class);
        when(imagePreproccessingService.convertToTiff(anyString(), anyString())).thenReturn(testFile);
        when(imagePreproccessingService.convertColorSpaces(anyString(), anyString(), anyString())).thenReturn(testFile);

        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String mockedJp2 = tmpFolder.resolve("albright_football_0082.jp2").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(mockedJp2);

            KakaduService service = new KakaduService();
            service.setColorFieldsService(colorFieldsService);
            service.setImagePreproccessingService(imagePreproccessingService);
            service.kduCompress(testFile, tmpFolder.resolve("albright_football_0082"), "");

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("kdu_compress", "-i", testFile, "-o", mockedJp2,
                            "Clevels=6", "Clayers=6",
                            "Cprecincts={256,256},{256,256},{128,128}", "Stiles={512,512}", "Corder=RPCL",
                            "ORGgen_plt=yes", "ORGtparts=R", "Cblk={64,64}", "Cuse_sop=yes", "Cuse_eph=yes",
                            "-flush_period", "1024", "-rate", "3", "-no_weights"))));
            verify(imagePreproccessingService, times(1))
                    .convertToTiff(testFile, "jpeg");
            verify(imagePreproccessingService, times(1))
                    .convertColorSpaces("RGB", "TrueColor", testFile);
        }
    }

    @Test
    public void testKduCompressYCbCrTif() throws Exception {
        String mockedTif = tmpFolder.resolve("mockedImage.tif").toString();
        Map<String, String> imageMetadata = Map.of(ColorFieldsService.PHOTOMETRIC_INTERPRETATION, "YCbCr");
        ColorFieldsService colorFieldsService = mock(ColorFieldsService.class);
        when(colorFieldsService.extractMetadataFields(anyString())).thenReturn(imageMetadata);
        when(colorFieldsService.identifyType(anyString())).thenReturn("TrueColor");
        ImagePreproccessingService imagePreproccessingService = mock(ImagePreproccessingService.class);
        when(imagePreproccessingService.convertToTiff(anyString(), anyString())).thenReturn(mockedTif);
        when(imagePreproccessingService.convertColorSpaces(anyString(), anyString(), anyString())).thenReturn(mockedTif);

        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String mockedJp2 = tmpFolder.resolve("mockedImage.jp2").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList())).thenReturn(mockedJp2);

            KakaduService service = new KakaduService();
            service.setColorFieldsService(colorFieldsService);
            service.setImagePreproccessingService(imagePreproccessingService);
            service.kduCompress(mockedTif, tmpFolder.resolve("mockedImage"), "");

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("kdu_compress", "-i", mockedTif, "-o", mockedJp2,
                            "Clevels=6", "Clayers=6",
                            "Cprecincts={256,256},{256,256},{128,128}", "Stiles={512,512}", "Corder=RPCL",
                            "ORGgen_plt=yes", "ORGtparts=R", "Cblk={64,64}", "Cuse_sop=yes", "Cuse_eph=yes",
                            "-flush_period", "1024", "-rate", "3", "-no_weights"))));

            verify(imagePreproccessingService, times(1))
                    .convertToTiff(mockedTif, "tiff");
            verify(imagePreproccessingService, times(1))
                    .convertColorSpaces("YCbCr", "TrueColor", mockedTif);
        }
    }

    @Test
    public void testKduCompressTiffWithPaletteTypeAndColorSpace() throws Exception {
        String mockedTif = tmpFolder.resolve("mockedImage.tif").toString();
        Map<String, String> imageMetadata = Map.of(ColorFieldsService.COLOR_SPACE, "RGB Palette");
        ColorFieldsService colorFieldsService = mock(ColorFieldsService.class);
        when(colorFieldsService.extractMetadataFields(anyString())).thenReturn(imageMetadata);
        when(colorFieldsService.identifyType(anyString())).thenReturn("Palette");
        ImagePreproccessingService imagePreproccessingService = mock(ImagePreproccessingService.class);
        when(imagePreproccessingService.convertToTiff(anyString(), anyString())).thenReturn(mockedTif);
        when(imagePreproccessingService.convertColorSpaces(anyString(), anyString(), anyString())).thenReturn(mockedTif);

        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String mockedJp2 = tmpFolder.resolve("mockedImage.jp2").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList())).thenReturn(mockedJp2);

            KakaduService service = new KakaduService();
            service.setColorFieldsService(colorFieldsService);
            service.setImagePreproccessingService(imagePreproccessingService);
            service.kduCompress(mockedTif, tmpFolder.resolve("mockedImage"), "");

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("kdu_compress", "-i", mockedTif, "-o", mockedJp2,
                            "Clevels=6", "Clayers=6", "Cprecincts={256,256},{256,256},{128,128}", "" +
                            "Stiles={512,512}", "Corder=RPCL", "ORGgen_plt=yes", "ORGtparts=R", "Cblk={64,64}",
                            "Cuse_sop=yes", "Cuse_eph=yes", "-flush_period", "1024", "-rate", "3", "-no_weights"))));
            verify(imagePreproccessingService, times(1))
                    .convertToTiff(mockedTif, "tiff");
            verify(imagePreproccessingService, times(1))
                    .convertColorSpaces("RGB Palette", "Palette", mockedTif);
        }
    }

    private void assertContains(String expected, String actual) {
        assertTrue(actual.contains(expected), "Expected string '" + expected + "' not found: " + actual);
    }
}
