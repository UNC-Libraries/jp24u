package JP2ImageConverter;

import JP2ImageConverter.services.ColorFieldsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ColorFieldsIT {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private ColorFieldsService service;

    @BeforeEach
    public void setup() throws Exception {
        System.setOut(new PrintStream(outputStreamCaptor));

        service = new ColorFieldsService();
    }

    @Test
    public void testIdentify() throws Exception {
        String testFile = "src/test/resources/P0024_0066.tif";

        String attributes = service.identify(testFile);
        assertContains("Dimensions: 5300x3841;", attributes);
        assertContains("Channels: gray", attributes);
        assertContains("Bit-depth: 16;", attributes);
        assertContains("Color Space: Gray;", attributes);
        assertContains("Type: Grayscale;", attributes);
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
        String attributes = outputStreamCaptor.toString();
        assertContains("PhotometricInterpretation:BlackIsZero", attributes);
        assertContains("MagickIdentify:", attributes);
        assertContains("Dimensions: 5300x3841;", attributes);
        assertContains("Channels: gray", attributes);
        assertContains("Bit-depth: 16;", attributes);
        assertContains("Color Space: Gray;", attributes);
        assertContains("Type: Grayscale;", attributes);
    }

    @Test
    public void testListOfImportFiles() throws Exception {
        String testFile = "src/test/resources/test_input.txt";

        service.fileListAllFields(testFile);
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

    @Test
    public void testListOfImportFilesWithNonexistentFileFail() throws Exception {
        String testFile = "src/test/resources/test_input_fail.txt";

        service.fileListAllFields(testFile);
        String attributes = outputStreamCaptor.toString();
        assertContains("ImageFileName:src/test/resources/E101_F8_0112.tif", attributes);
        assertContains("ICCProfileName:Adobe RGB", attributes);
        assertContains("PhotometricInterpretation:RGB", attributes);
        assertContains("Channels: gray", attributes);
        assertContains("Dimensions: 2600x3650;", attributes);
        assertContains("Channels: srgb", attributes);
        assertContains("Bit-depth: 8;", attributes);
        assertContains("Color Space: sRGB;", attributes);
        assertContains("Type: TrueColor;", attributes);

        assertContains("src/test/resources/test.tif does not exist.", attributes);

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

    private void assertContains(String expected, String actual) {
        assertTrue(actual.contains(expected), "Expected string '" + expected + "' not found: " + actual);
    }
}
