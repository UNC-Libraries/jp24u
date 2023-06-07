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

    // FILE_MODIFIED_DATE and FileModifiedDate tend to change when rerunning tests,
    // so I avoided giving them actual values
    @Test
    public void testColorFields() throws Exception {
        String testFile = "src/test/resources/E101_F8_0112.tif";

        Map<String, String> fields = service.colorFields(testFile);

        assertTrue(fields.containsValue(testFile));
        assertTrue(fields.containsValue("57001526 bytes"));
        assertTrue(fields.containsKey(ColorFieldsService.FILE_MODIFIED_DATE));
        assertTrue(fields.containsValue("2021:08:30 19:56:48"));
        assertTrue(fields.containsValue("2021:08:30 19:56:48"));
        assertTrue(fields.containsValue("Adobe RGB (1998)"));
        assertTrue(fields.containsValue("RGB"));
        assertTrue(fields.containsValue("Unknown (R03)"));
        assertTrue(fields.containsValue("RGB"));
    }

    @Test
    public void testMissingColorFields() throws Exception {
        String testFile = "src/test/resources/P0024_0066.tif";

        // PhotometricInterpretation is never missing
        Map<String, String> fields = service.colorFields(testFile);

        assertTrue(fields.containsValue(testFile));
        assertTrue(fields.containsValue("40736840 bytes"));
        assertTrue(fields.containsKey(ColorFieldsService.FILE_MODIFIED_DATE));
        assertTrue(fields.containsValue(null));
        assertTrue(fields.containsValue("2013:06:25 14:51:58"));
        assertTrue(fields.containsValue(null));
        assertTrue(fields.containsValue(null));
        assertTrue(fields.containsValue(null));
        assertTrue(fields.containsValue("BlackIsZero"));
    }

    @Test
    public void testIdentify() throws Exception {
        String testFile = "src/test/resources/P0024_0066.tif";

        String testAttributes = "\"Dimensions: 5300x3841;Channels: gray;Bit-depth: 16;Alpha channel: False;" +
                "Color Space: Gray;Profiles: 8bim,xmp;ICC Profile: ;ICM Profile: ;\"";
        String attributes = service.identify(testFile);
        assertEquals(testAttributes, attributes);
    }

    @Test
    public void testAllFields() throws Exception {
        String testFile = "src/test/resources/P0024_0066.tif";

        service.listFields(testFile);
        assertTrue(outputStreamCaptor.toString().contains("ImageFileName:src/test/resources/P0024_0066.tif\t" +
                "FileSize:40736840 bytes\t"));
        assertTrue(outputStreamCaptor.toString().contains("DateTimeOriginal:null\t" +
                "DateTimeDigitized:2013:06:25 14:51:58\tICCProfileName:null\tColorSpace:null\t" +
                "InteropIndex:null\tPhotometricInterpretation:BlackIsZero\tMagickIdentify:\"Dimensions: 5300x3841;" +
                "Channels: gray;Bit-depth: 16;Alpha channel: False;Color Space: Gray;Profiles: 8bim,xmp;" +
                "ICC Profile: ;ICM Profile: ;\"\t\n"));
    }

    @Test
    public void testListOfImportFiles() throws Exception {
        String testFile = "src/test/resources/test_input.txt";

        service.fileListAllFields(testFile);
        assertTrue(outputStreamCaptor.toString().contains("ImageFileName:src/test/resources/E101_F8_0112.tif\t" +
                "FileSize:57001526 bytes\t"));
        assertTrue(outputStreamCaptor.toString().contains(
                "DateTimeOriginal:2021:08:30 19:56:48\tDateTimeDigitized:2021:08:30 19:56:48\t" +
                "ICCProfileName:Adobe RGB (1998)\tColorSpace:RGB\tInteropIndex:Unknown (R03)\t" +
                "PhotometricInterpretation:RGB\tMagickIdentify:\"Dimensions: 2600x3650;Channels: srgb;" +
                "Bit-depth: 16;Alpha channel: False;Color Space: sRGB;Profiles: icc,xmp;" +
                "ICC Profile: Adobe RGB (1998);ICM Profile: ;Dimensions: 114x160;Channels: srgb;Bit-depth: 8;" +
                "Alpha channel: False;Color Space: sRGB;Profiles: ;ICC Profile: ;ICM Profile: ;\"\t\n"));
        assertTrue(outputStreamCaptor.toString().contains("ImageFileName:src/test/resources/P0024_0066.tif\t" +
                "FileSize:40736840 bytes\t"));
        assertTrue(outputStreamCaptor.toString().contains("DateTimeOriginal:null\t" +
                "DateTimeDigitized:2013:06:25 14:51:58\t" +
                "ICCProfileName:null\tColorSpace:null\tInteropIndex:null\tPhotometricInterpretation:BlackIsZero\t" +
                "MagickIdentify:\"Dimensions: 5300x3841;Channels: gray;Bit-depth: 16;" +
                "Alpha channel: False;Color Space: Gray;Profiles: 8bim,xmp;ICC Profile: ;ICM Profile: ;\"\t\n"));
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
        assertTrue(outputStreamCaptor.toString().contains("ImageFileName:src/test/resources/E101_F8_0112.tif\t" +
                "FileSize:57001526 bytes\t"));
        assertTrue(outputStreamCaptor.toString().contains(
                "DateTimeOriginal:2021:08:30 19:56:48\tDateTimeDigitized:2021:08:30 19:56:48\t" +
                "ICCProfileName:Adobe RGB (1998)\tColorSpace:RGB\tInteropIndex:Unknown (R03)\t" +
                "PhotometricInterpretation:RGB\tMagickIdentify:\"Dimensions: 2600x3650;Channels: srgb;" +
                "Bit-depth: 16;Alpha channel: False;Color Space: sRGB;Profiles: icc,xmp;" +
                "ICC Profile: Adobe RGB (1998);ICM Profile: ;Dimensions: 114x160;Channels: srgb;Bit-depth: 8;" +
                "Alpha channel: False;Color Space: sRGB;Profiles: ;ICC Profile: ;ICM Profile: ;\"\t\n"));
        assertTrue(outputStreamCaptor.toString().contains("src/test/resources/test.tif does not exist."));
        assertTrue(outputStreamCaptor.toString().contains("ImageFileName:src/test/resources/P0024_0066.tif\t" +
                "FileSize:40736840 bytes\t"));
        assertTrue(outputStreamCaptor.toString().contains(
                "DateTimeOriginal:null\tDateTimeDigitized:2013:06:25 14:51:58\t" +
                "ICCProfileName:null\tColorSpace:null\tInteropIndex:null\tPhotometricInterpretation:BlackIsZero\t" +
                "MagickIdentify:\"Dimensions: 5300x3841;Channels: gray;Bit-depth: 16;" +
                "Alpha channel: False;Color Space: Gray;Profiles: 8bim,xmp;ICC Profile: ;ICM Profile: ;\"\t\n"));
        assertTrue(outputStreamCaptor.toString().contains("Number of Files Processed: 2"));
        assertTrue(outputStreamCaptor.toString().contains("Total Overall Runtime: "));
        assertTrue(outputStreamCaptor.toString().contains("Average Runtime per File: "));
        assertTrue(outputStreamCaptor.toString().contains("Total Exif Runtime: "));
        assertTrue(outputStreamCaptor.toString().contains("Total ImageMagick Identify Runtime: "));
    }
}
