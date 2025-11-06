package JP2ImageConverter.services;

import JP2ImageConverter.util.CommandUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.MockitoAnnotations.openMocks;

public class ColorFieldsServiceTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private AutoCloseable closeable;

    private ColorFieldsService service;

    @BeforeEach
    public void setup() throws Exception {
        closeable = openMocks(this);
        System.setOut(new PrintStream(outputStreamCaptor));

        service = new ColorFieldsService();
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Test
    public void testExtractMetadataFields() throws Exception {
        String testFile = "src/test/resources/E101_F8_0112.tif";

        Map<String, String> testFields = new HashMap<>();
        testFields.put(ColorFieldsService.IMAGE_FILE_NAME, "src/test/resources/E101_F8_0112.tif");
        testFields.put(ColorFieldsService.FILE_SIZE, "57001526 bytes");
        testFields.put(ColorFieldsService.DATE_TIME_ORIGINAL, "2021:08:30 19:56:48");
        testFields.put(ColorFieldsService.DATE_TIME_DIGITIZED, "2021:08:30 19:56:48");
        testFields.put(ColorFieldsService.ICC_PROFILE_NAME, "Adobe RGB (1998)");
        testFields.put(ColorFieldsService.COLOR_SPACE, "RGB");
        testFields.put(ColorFieldsService.A_TO_B0, null);
        testFields.put(ColorFieldsService.INTEROP_INDEX, "Unknown (R03)");
        testFields.put(ColorFieldsService.PHOTOMETRIC_INTERPRETATION, "RGB");

        Map<String, String> fields = service.extractMetadataFields(testFile);
        assertMapContainsExpected(testFields, fields);
    }

    public static void assertMapContainsExpected(Map<String, String> expected, Map<String, String> actual) {
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            assertTrue(actual.containsKey(entry.getKey()), "Expected key not found: " + entry.getKey());
            assertEquals(entry.getValue(), actual.get(entry.getKey()), "Value mismatch for key: " + entry.getKey());
        }
    }

    @Test
    public void testMissingColorFields() throws Exception {
        String testFile = "src/test/resources/P0024_0066.tif";

        // PhotometricInterpretation is never missing
        Map<String,String> testFields = new HashMap<>();
        testFields.put(ColorFieldsService.IMAGE_FILE_NAME, testFile);
        testFields.put(ColorFieldsService.FILE_SIZE, "40736840 bytes");
        testFields.put(ColorFieldsService.DATE_TIME_ORIGINAL, null);
        testFields.put(ColorFieldsService.DATE_TIME_DIGITIZED, "2013:06:25 14:51:58");
        testFields.put(ColorFieldsService.ICC_PROFILE_NAME, null);
        testFields.put(ColorFieldsService.COLOR_SPACE, null);
        testFields.put(ColorFieldsService.A_TO_B0, null);
        testFields.put(ColorFieldsService.INTEROP_INDEX, null);
        testFields.put(ColorFieldsService.PHOTOMETRIC_INTERPRETATION, "BlackIsZero");

        Map<String, String> fields = service.extractMetadataFields(testFile);
        assertMapContainsExpected(testFields, fields);
    }

    @Test
    public void testIdentify() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = "src/test/resources/OP20459_1_TremorsKelleyandtheCowboys.tif";
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn("Dimensions: 5300x3841;Channels: gray;Bit-depth: 16;Alpha channel: False;" +
                            "Color Space: Gray;Profiles: 8bim,xmp;ICC Profile: ;ICM Profile: ;Type: Grayscale;");

            ColorFieldsService service = new ColorFieldsService();
            String attributes = service.identify(testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("identify", "-quiet", "-format",
                            "Dimensions: %wx%h;Channels: %[channels];Bit-depth: %[bit-depth];" +
                            "Alpha channel: %A;Color Space: %[colorspace];Profiles: %[profiles];" +
                            "ICC Profile: %[profile:icc];ICM Profile: %[profile:icm];Type: %[type];", testFile))));
            assertContains("Dimensions: 5300x3841;", attributes);
            assertContains("Channels: gray", attributes);
            assertContains("Bit-depth: 16;", attributes);
            assertContains("Color Space: Gray;", attributes);
            assertContains("Type: Grayscale;", attributes);
        }
    }

    private void assertContains(String expected, String actual) {
        assertTrue(actual.contains(expected), "Expected string '" + expected + "' not found: " + actual);
    }

    @Test
    public void testIdentifyImageType() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = "src/test/resources/P0024_0066.tif";
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn("Grayscale");

            ColorFieldsService service = new ColorFieldsService();
            String attributes = service.identifyType(testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("identify", "-quiet", "-format", "%[type]", testFile))));
            assertContains("Grayscale", attributes);
        }
    }

    @Test
    public void testAllFields() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = "src/test/resources/P0024_0066.tif";
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn("ICCProfileName:null\tColorSpace:null\tInteropIndex:null\t" +
                            "PhotometricInterpretation:BlackIsZero\tMagickIdentify:\"Dimensions: 5300x3841;" +
                            "Channels: gray  1.0;Bit-depth: 16;Alpha channel: Undefined;Color Space: Gray;" +
                            "Profiles: 8bim,xmp;ICC Profile: ;ICM Profile: ;Type: Grayscale;\"\t\n");

            ColorFieldsService service = new ColorFieldsService();
            service.listFields(testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("identify", "-quiet", "-format",
                            "Dimensions: %wx%h;Channels: %[channels];Bit-depth: %[bit-depth];" +
                                    "Alpha channel: %A;Color Space: %[colorspace];Profiles: %[profiles];" +
                                    "ICC Profile: %[profile:icc];ICM Profile: %[profile:icm];Type: %[type];",
                            testFile))));
            assertTrue(outputStreamCaptor.toString().contains("ImageFileName:src/test/resources/P0024_0066.tif"));
            String attributes = outputStreamCaptor.toString();
            assertContains("PhotometricInterpretation:BlackIsZero", attributes);
            assertContains("MagickIdentify:", attributes);
            assertContains("Dimensions: 5300x3841;", attributes);
            assertContains("Channels: gray", attributes);
            assertContains("Bit-depth: 16;", attributes);
            assertContains("Color Space: Gray;", attributes);
            assertContains("Type: Grayscale;", attributes);
        }
    }

    @Test
    public void testListOfImportFiles() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            String testFile = "src/test/resources/test_input.txt";;
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn("ImageFileName:src/test/resources/E101_F8_0112.tif\tFileSize:57001526 bytes\t" +
                            "FileModifiedDate:Thu May 18 17:00:05 -04:00 2023\tDateTimeOriginal:2021:08:30 19:56:48\t" +
                            "DateTimeDigitized:2021:08:30 19:56:48\tICCProfileName:Adobe RGB (1998)\t" +
                            "ColorSpace:RGB\tAToB0:null\tInteropIndex:Unknown (R03)\tPhotometricInterpretation:RGB\t" +
                            "Orientation:Top, left side (Horizontal / normal)\t" +
                            "MagickIdentify:\"Dimensions: 2600x3650;Channels: srgb;Bit-depth: 16;Alpha channel: False;" +
                            "Color Space: sRGB;Profiles: icc,iptc,xmp;ICC Profile: Adobe RGB (1998);ICM Profile: ;" +
                            "Type: TrueColor;Dimensions: 114x160;Channels: srgb;Bit-depth: 8;Alpha channel: False;" +
                            "Color Space: sRGB;Profiles: ;ICC Profile: ;ICM Profile: ;Type: TrueColor;\"\t\n" +
                            "ImageFileName:src/test/resources/P0024_0066.tif\tFileSize:40736840 bytes\t" +
                            "FileModifiedDate:Thu May 18 17:00:05 -04:00 2023\tDateTimeOriginal:null\t" +
                            "DateTimeDigitized:2013:06:25 14:51:58\tICCProfileName:null\tColorSpace:null\t" +
                            "AToB0:null\tInteropIndex:null\tPhotometricInterpretation:BlackIsZero\t" +
                            "Orientation:Top, left side (Horizontal / normal)\tMagickIdentify:\"" +
                            "Dimensions: 5300x3841;Channels: gray;Bit-depth: 16;Alpha channel: False;" +
                            "Color Space: Gray;Profiles: 8bim,xmp;ICC Profile: ;ICM Profile: ;Type: Grayscale;");

            ColorFieldsService service = new ColorFieldsService();
            service.fileListAllFields(testFile);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("identify", "-quiet", "-format",
                            "Dimensions: %wx%h;Channels: %[channels];Bit-depth: %[bit-depth];" +
                                    "Alpha channel: %A;Color Space: %[colorspace];Profiles: %[profiles];" +
                                    "ICC Profile: %[profile:icc];ICM Profile: %[profile:icm];Type: %[type];",
                            "src/test/resources/E101_F8_0112.tif"))));
            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("identify", "-quiet", "-format",
                            "Dimensions: %wx%h;Channels: %[channels];Bit-depth: %[bit-depth];" +
                                    "Alpha channel: %A;Color Space: %[colorspace];Profiles: %[profiles];" +
                                    "ICC Profile: %[profile:icc];ICM Profile: %[profile:icm];Type: %[type];",
                            "src/test/resources/P0024_0066.tif"))));
            String attributes = outputStreamCaptor.toString();
            assertContains("ImageFileName:src/test/resources/E101_F8_0112.tif", attributes);
            assertContains("ICCProfileName:Adobe RGB", attributes);
            assertContains("PhotometricInterpretation:RGB", attributes);
            assertContains("Dimensions: 2600x3650;", attributes);
            assertContains("Channels: srgb", attributes);
            assertContains("Bit-depth: 8;", attributes);
            assertContains("Color Space: sRGB;", attributes);
            assertContains("Type: TrueColor;", attributes);

            assertContains("ImageFileName:src/test/resources/P0024_0066.tif", attributes);
            assertContains("Dimensions: 5300x3841;", attributes);
            assertContains("Channels: gray", attributes);
            assertContains("Bit-depth: 16;", attributes);
            assertContains("Color Space: Gray;", attributes);
            assertContains("Type: Grayscale;", attributes);
            assertContains("Number of Files Processed: 2", attributes);
            assertContains("Total Overall Runtime: ", attributes);
            assertContains("Average Runtime per File: ", attributes);
            assertContains("Total Exif Runtime: ", attributes);
            assertContains("Total ImageMagick Identify Runtime: ", attributes);
        }
    }
}
