package JP2ImageConverter.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ColorFieldsServiceTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private ColorFieldsService service;

    @BeforeEach
    public void setup() throws Exception {
        System.setOut(new PrintStream(outputStreamCaptor));

        service = new ColorFieldsService();
    }

    // if all the tests stop passing, you probably just need to update the FILE_MODIFIED_DATE and FileModifiedDate
    @Test
    public void testColorFields() throws Exception {
        String testFile = "src/test/resources/E101_F8_0112.tif";

        Map<String, String> testFields = new LinkedHashMap<>();
        testFields.put(ColorFieldsService.IMAGE_FILE_NAME, "src/test/resources/E101_F8_0112.tif");
        testFields.put(ColorFieldsService.FILE_SIZE, "57001526 bytes");
        testFields.put(ColorFieldsService.FILE_MODIFIED_DATE, "Thu May 18 17:00:05 -04:00 2023");
        testFields.put(ColorFieldsService.DATE_TIME_ORIGINAL, "2021:08:30 19:56:48");
        testFields.put(ColorFieldsService.DATE_TIME_DIGITIZED, "2021:08:30 19:56:48");
        testFields.put(ColorFieldsService.ICC_PROFILE_NAME, "Adobe RGB (1998)");
        testFields.put(ColorFieldsService.COLOR_SPACE, "RGB");
        testFields.put(ColorFieldsService.INTEROP_INDEX, "Unknown (R03)");
        testFields.put(ColorFieldsService.PHOTOMETRIC_INTERPRETATION, "RGB");

        Map<String, String> fields = service.colorFields(testFile);
        assertEquals(testFields, fields);
    }

    @Test
    public void testMissingColorFields() throws Exception {
        String testFile = "src/test/resources/P0024_0066.tif";

        // PhotometricInterpretation is never missing
        Map<String,String> testFields = new LinkedHashMap<>();
        testFields.put(ColorFieldsService.IMAGE_FILE_NAME, testFile);
        testFields.put(ColorFieldsService.FILE_SIZE, "40736840 bytes");
        testFields.put(ColorFieldsService.FILE_MODIFIED_DATE, "Thu May 18 17:00:05 -04:00 2023");
        testFields.put(ColorFieldsService.DATE_TIME_ORIGINAL, null);
        testFields.put(ColorFieldsService.DATE_TIME_DIGITIZED, "2013:06:25 14:51:58");
        testFields.put(ColorFieldsService.ICC_PROFILE_NAME, null);
        testFields.put(ColorFieldsService.COLOR_SPACE, null);
        testFields.put(ColorFieldsService.INTEROP_INDEX, null);
        testFields.put(ColorFieldsService.PHOTOMETRIC_INTERPRETATION, "BlackIsZero");

        Map<String, String> fields = service.colorFields(testFile);
        assertEquals(testFields, fields);
    }

    @Test
    public void testIdentify() throws Exception {
        String testFile = "src/test/resources/P0024_0066.tif";

        String testAttributes = "\"Dimensions: 5300x3841;Channels: gray  1.0;Bit-depth: 16;Alpha channel: Undefined;" +
                "Color Space: Gray;Profiles: 8bim,xmp;ICC Profile: ;ICM Profile: ;Type: Grayscale;\"";
        String attributes = service.identify(testFile);
        assertEquals(testAttributes, attributes);
    }

    @Test
    public void testIdentifyImageType() throws Exception {
        String testFile = "src/test/resources/P0024_0066.tif";

        String testImageType = "Grayscale";
        String imageType = service.identifyType(testFile);
        assertEquals(testImageType, imageType);
    }

    @Test
    public void testAllFields() throws Exception {
        String testFile = "src/test/resources/P0024_0066.tif";

        service.listFields(testFile);
        assertTrue(outputStreamCaptor.toString().contains("ImageFileName:src/test/resources/P0024_0066.tif"));
        String testOutput = "ICCProfileName:null\tColorSpace:null\t" +
                "InteropIndex:null\tPhotometricInterpretation:BlackIsZero\tMagickIdentify:\"Dimensions: 5300x3841;" +
                "Channels: gray  1.0;Bit-depth: 16;Alpha channel: Undefined;Color Space: Gray;Profiles: 8bim,xmp;" +
                "ICC Profile: ;ICM Profile: ;Type: Grayscale;\"\t\n";
        assertTrue(outputStreamCaptor.toString().contains(testOutput));
    }

    @Test
    public void testListOfImportFiles() throws Exception {
        String testFile = "src/test/resources/test_input.txt";

        service.fileListAllFields(testFile);
        assertTrue(outputStreamCaptor.toString().contains("ImageFileName:src/test/resources/E101_F8_0112.tif"));
        assertTrue(outputStreamCaptor.toString().contains(
                "ICCProfileName:Adobe RGB (1998)\tColorSpace:RGB\tInteropIndex:Unknown (R03)\t" +
                "PhotometricInterpretation:RGB\tMagickIdentify:\"Dimensions: 2600x3650;Channels: srgb  3.0;" +
                "Bit-depth: 16;Alpha channel: Undefined;Color Space: sRGB;Profiles: icc,iptc,xmp;" +
                "ICC Profile: Adobe RGB (1998);ICM Profile: ;Type: TrueColor;Dimensions: 114x160;Channels: srgb  3.0;" +
                "Bit-depth: 8;Alpha channel: Undefined;Color Space: sRGB;Profiles: ;" +
                "ICC Profile: ;ICM Profile: ;Type: TrueColor;\"\t\n"));
        assertTrue(outputStreamCaptor.toString().contains("ImageFileName:src/test/resources/P0024_0066.tif"));
        assertTrue(outputStreamCaptor.toString().contains(
                "ICCProfileName:null\tColorSpace:null\tInteropIndex:null\tPhotometricInterpretation:BlackIsZero\t" +
                "MagickIdentify:\"Dimensions: 5300x3841;Channels: gray  1.0;Bit-depth: 16;" +
                "Alpha channel: Undefined;Color Space: Gray;Profiles: 8bim,xmp;ICC Profile: ;ICM Profile: ;" +
                "Type: Grayscale;\"\t\n"));
        assertTrue(outputStreamCaptor.toString().contains("Number of Files Processed: 2"));
        assertTrue(outputStreamCaptor.toString().contains("Total Overall Runtime: "));
        assertTrue(outputStreamCaptor.toString().contains("Average Runtime per File: "));
        assertTrue(outputStreamCaptor.toString().contains("Total Exif Runtime: "));
        assertTrue(outputStreamCaptor.toString().contains("Total ImageMagick Identify Runtime: "));
    }

    @Test
    public void testListOfImportFilesWithNonexistentFileFail() throws Exception {
        String testFile = "src/test/resources/test_input_fail.txt";

        service.fileListAllFields(testFile);
        assertTrue(outputStreamCaptor.toString().contains("ImageFileName:src/test/resources/E101_F8_0112.tif"));
        assertTrue(outputStreamCaptor.toString().contains(
                "ICCProfileName:Adobe RGB (1998)\tColorSpace:RGB\tInteropIndex:Unknown (R03)\t" +
                "PhotometricInterpretation:RGB\tMagickIdentify:\"Dimensions: 2600x3650;Channels: srgb  3.0;" +
                "Bit-depth: 16;Alpha channel: Undefined;Color Space: sRGB;Profiles: icc,iptc,xmp;" +
                "ICC Profile: Adobe RGB (1998);ICM Profile: ;Type: TrueColor;Dimensions: 114x160;" +
                "Channels: srgb  3.0;Bit-depth: 8;Alpha channel: Undefined;Color Space: sRGB;" +
                "Profiles: ;ICC Profile: ;ICM Profile: ;Type: TrueColor;\"\t\n"));
        assertTrue(outputStreamCaptor.toString().contains("src/test/resources/test.tif does not exist."));
        assertTrue(outputStreamCaptor.toString().contains("ImageFileName:src/test/resources/P0024_0066.tif"));
        assertTrue(outputStreamCaptor.toString().contains(
                "ICCProfileName:null\tColorSpace:null\tInteropIndex:null\tPhotometricInterpretation:BlackIsZero\t" +
                "MagickIdentify:\"Dimensions: 5300x3841;Channels: gray  1.0;Bit-depth: 16;" +
                "Alpha channel: Undefined;Color Space: Gray;Profiles: 8bim,xmp;ICC Profile: ;ICM Profile: ;" +
                "Type: Grayscale;\"\t\n"));
        assertTrue(outputStreamCaptor.toString().contains("Number of Files Processed: 2"));
        assertTrue(outputStreamCaptor.toString().contains("Total Overall Runtime: "));
        assertTrue(outputStreamCaptor.toString().contains("Average Runtime per File: "));
        assertTrue(outputStreamCaptor.toString().contains("Total Exif Runtime: "));
        assertTrue(outputStreamCaptor.toString().contains("Total ImageMagick Identify Runtime: "));
    }
}
