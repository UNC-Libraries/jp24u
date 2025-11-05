package JP2ImageConverter.services;

import JP2ImageConverter.util.CommandUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.MockitoAnnotations.openMocks;

public class ImagePreprocessingServiceTest {
    @TempDir
    public Path tmpFolder;

    private AutoCloseable closeable;

    private ImagePreproccessingService service;

    @BeforeEach
    public void setup() throws Exception {
        closeable = openMocks(this);

        service = new ImagePreproccessingService();
        service.tmpFilesDir = tmpFolder;
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Test
    public void testSetColorSpaceAndRemoveProfileWithIm() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = "src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif";
            String tempTif = tmpFolder.resolve("OP20459_1_TremorsKelleyandtheCowboys.tif").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(tempTif);

            ImagePreproccessingService service = new ImagePreproccessingService();
            String outputFile = service.setColorSpaceRemoveProfileWithIm(testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("gm", "convert", "-auto-orient", testFile, "-colorspace", "rgb",
                            "+profile", "\"*\"", outputFile))));
        }
    }

    @Test
    public void testSetTypeAndColorspaceWithGm() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = tmpFolder.resolve("mockedImage.tif").toString();
            String tempTif = tmpFolder.resolve("mockedImage.tif").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(tempTif);

            ImagePreproccessingService service = new ImagePreproccessingService();
            String outputFile = service.setTypeAndColorspaceWithGm(testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("gm", "convert", "-auto-orient", "-type", "TrueColor",
                            "-colorspace", "sRGB", testFile, outputFile))));
        }
    }

    @Test
    public void testColorSpaceWithIm() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = tmpFolder.resolve("mockedImage.tif").toString();
            String tempTif = tmpFolder.resolve("mockedImage.tif").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(tempTif);

            ImagePreproccessingService service = new ImagePreproccessingService();
            String outputFile = service.setColorSpaceWithIm(testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("convert", "-auto-orient", testFile, "-colorspace", "sRGB",
                            outputFile))));
        }
    }

    @Test
    public void testConvertToPpmWithIm() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = tmpFolder.resolve("mockedImage.jpeg").toString();
            String tempTif = tmpFolder.resolve("mockedImage.tif").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(tempTif);

            ImagePreproccessingService service = new ImagePreproccessingService();
            String outputFile = service.convertToPpmWithIm(testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("convert", "-auto-orient", testFile, outputFile))));
        }
    }

    @Test
    public void testConvertToPpmWithDcraw() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = tmpFolder.resolve("mockedImage.rw2").toString();
            String tempTif = tmpFolder.resolve("mockedImage.ppm").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(tempTif);

            ImagePreproccessingService service = new ImagePreproccessingService();
            String outputFile = service.convertToPpmWithDcraw(testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommandWriteToFile(
                    new ArrayList<>(Arrays.asList("dcraw", "-c", "-w", testFile)), outputFile));
        }
    }

    @Test
    public void testConvertToTifWithGm() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = tmpFolder.resolve("mockedImage.png").toString();
            String tempTif = tmpFolder.resolve("mockedImage.tif").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(tempTif);

            ImagePreproccessingService service = new ImagePreproccessingService();
            String outputFile = service.convertToTifWithGm(testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("gm", "convert", "-auto-orient", testFile + "[0]", outputFile))));
        }
    }

    private void assertContains(String expected, String actual) {
        assertTrue(actual.contains(expected), "Expected string '" + expected + "' not found: " + actual);
    }

    @Test
    public void testFlattenSetColorspaceConvertToTifWithIm() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = tmpFolder.resolve("mockedImage.psd").toString();
            String tempTif = tmpFolder.resolve("mockedImage.tif").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(tempTif);

            ImagePreproccessingService service = new ImagePreproccessingService();
            String outputFile = service.flattenSetColorspaceConvertToTifWithIm(testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("convert", "-auto-orient", testFile + "[0]",
                            "-colorspace", "sRGB", outputFile))));
        }
    }

    @Test
    public void testConvertToTifWithIm() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = tmpFolder.resolve("mockedImage.jp2").toString();
            String tempTif = tmpFolder.resolve("mockedImage.tif").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(tempTif);

            ImagePreproccessingService service = new ImagePreproccessingService();
            String outputFile = service.convertToTifWithIm(testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("convert", "-auto-orient", testFile, outputFile))));
        }
    }

    @Test
    public void testConvertToJpgWithExiftool() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = tmpFolder.resolve("mockedImage.nef").toString();
            String tempJpeg = tmpFolder.resolve("mockedImage.jpeg").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(tempJpeg);

            ImagePreproccessingService service = new ImagePreproccessingService();
            String outputFile = service.convertToJpgWithExiftool(testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommandWriteToFile(
                    new ArrayList<>(Arrays.asList("exiftool", "-b", "-JpgFromRaw", testFile)), outputFile));
            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("exiftool", "-overwrite_original", "-tagsfromfile",
                            testFile, "-orientation", outputFile))));
        }
    }

    @Test
    public void testConvertToPpmWithGm() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = tmpFolder.resolve("mockedImage.cr2").toString();
            String tempPpm = tmpFolder.resolve("mockedImage.ppm").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(tempPpm);

            ImagePreproccessingService service = new ImagePreproccessingService();
            String outputFile = service.convertToPpmWithGm(testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("gm", "convert", "-auto-orient",
                            testFile, outputFile))));
        }
    }

    @Test
    public void testConvertToTifHighestResolutionWithGm() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = tmpFolder.resolve("mockedImage.pcd").toString();
            String tempTif = tmpFolder.resolve("mockedImage.tif").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(tempTif);

            ImagePreproccessingService service = new ImagePreproccessingService();
            String outputFile = service.convertToTifHighestResolutionWithGm(testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("gm", "convert", "-auto-orient",
                            testFile + "[6]", outputFile))));
        }
    }

    @Test
    public void testImagePreprocessingPict() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = tmpFolder.resolve("mockedImage.pct").toString();
            String tempTif = tmpFolder.resolve("mockedImage.tif").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(tempTif);

            ImagePreproccessingService service = new ImagePreproccessingService();
            String outputFile = service.convertToTiff(testFile, "");

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("gm", "convert", "-auto-orient",
                            testFile + "[0]", outputFile))));
        }
    }

    @Test
    public void testImagePreprocessingTiff() throws Exception {
        String testFile = "src/test/resources/E101_F8_0112.tif";

        var tempTif = service.convertToTiff(testFile, "");

        assertTrue(Files.exists(Paths.get(tempTif)));
    }

    @Test
    public void testConvertColorspace() throws Exception {
        String testFile = "src/test/resources/IMG_3444.pct.tif";

        String result = service.convertColorSpaces("rgb", "truecolor", testFile);

        assertEquals(testFile, result);
    }

    @Test
    public void testConvertUnusualColorspace() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = tmpFolder.resolve("mockedImage.tif").toString();
            String tempTif = tmpFolder.resolve("mockedImage.tif").toString();
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(tempTif);

            ImagePreproccessingService service = new ImagePreproccessingService();
            String outputFile = service.convertColorSpaces("cmyk", "colorseparation", testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("gm", "convert", "-auto-orient", testFile, "-colorspace", "rgb",
                            "+profile", "\"*\"", outputFile))));
        }
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
