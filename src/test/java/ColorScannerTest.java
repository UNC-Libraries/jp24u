import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author krwong
 */
public class ColorScannerTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    ColorScanner colorScanner = new ColorScanner();

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    public void testMultipleArgumentsFail() throws Exception {
        String[] args = new String[2];
        args[0] = "src/test/resources/lorem_ipsum.txt";
        args[1] = "test";

        ColorScanner.main(args);
        assertEquals("Error: Please input an argument.", outputStreamCaptor.toString().trim());
    }

    @Test
    public void testNoFileArgFail() throws Exception {
        String[] args = new String[1];
        args[0] = " ";

        ColorScanner.main(args);
        assertEquals("Error: Please input an argument.", outputStreamCaptor.toString().trim());
    }

    @Test
    public void testNonexistentFileFail() throws Exception {
        String testFile = "src/test/resources/test.txt";
        String[] args = new String[1];
        args[0] = testFile;

        colorScanner.main(args);
        assertEquals("Error: src/test/resources/test.txt does not exist.",
                outputStreamCaptor.toString().trim());
    }

    @Test
    public void testListInputNonexistentFileFail() throws Exception {
        String testFile = "src/test/resources/test.txt";
        String[] args = new String[2];
        args[0] = "-list";
        args[1] = testFile;

        colorScanner.main(args);
        assertEquals("Error: src/test/resources/test.txt does not exist.", outputStreamCaptor.toString().trim());
    }

    @Test
    public void testColorFields() throws Exception {
        String testFile = "src/test/resources/E101_F8_0112.tif";

        List<String> testFields = new ArrayList<>();
        testFields.add("src/test/resources/E101_F8_0112.tif");
        testFields.add("ICCProfileName:Adobe RGB (1998)");
        testFields.add("ColorSpace:RGB ");
        testFields.add("InteropIndex:Unknown (R03)");
        testFields.add("PhotometricInterpretation:RGB");

        List<String> fields = colorScanner.colorFields(testFile);
        assertEquals(testFields, fields);
    }

    @Test
    public void testMissingColorFields() throws Exception {
        String testFile = "src/test/resources/P0024_0066.tif";

        //PhotometricInterpretation is never missing
        List<String> testFields = new ArrayList<>();
        testFields.add("src/test/resources/P0024_0066.tif");
        testFields.add("ICCProfileName:null");
        testFields.add("ColorSpace:null");
        testFields.add("InteropIndex:null");
        testFields.add("PhotometricInterpretation:BlackIsZero");

        List<String> fields = colorScanner.colorFields(testFile);
        assertEquals(testFields, fields);
    }

    @Test
    public void testIdentify() throws Exception {
        String testFile = "src/test/resources/P0024_0066.tif";

        String testAttributes = "\"Dimensions: 5300x3841;Channels: gray;Bit-depth: 16;Alpha channel: False;" +
                "Color Space: Gray;Profiles: 8bim,xmp;ICC Profile: ;ICM Profile: ;\"";
        String attributes = colorScanner.identify(testFile);
        assertEquals(testAttributes, attributes);
    }

    @Test
    public void testAllFields() throws Exception {
        String testFile = "src/test/resources/P0024_0066.tif";
        String[] args = new String[1];
        args[0] = testFile;

        colorScanner.main(args);
        assertTrue(outputStreamCaptor.toString().contains("src/test/resources/P0024_0066.tif\tICCProfileName:null\tColorSpace:null\tInteropIndex:null\t" +
                "PhotometricInterpretation:BlackIsZero\t\"Dimensions: 5300x3841;Channels: gray;Bit-depth: 16;" +
                "Alpha channel: False;Color Space: Gray;Profiles: 8bim,xmp;ICC Profile: ;ICM Profile: ;\""));
    }

    @Test
    public void testListOfImportFiles() throws IOException {
        String testFile = "src/test/resources/test_input.txt";

        List<String> testListOfImportFiles = new ArrayList<>();
        testListOfImportFiles.add("src/test/resources/E101_F8_0112.tif");
        testListOfImportFiles.add("src/test/resources/P0024_0066.tif");

        List<String> listOfImportFiles = colorScanner.readFileInList(testFile);
        assertEquals(testListOfImportFiles, listOfImportFiles);
    }

    @Test
    public void testListOfImportFilesWithNonexistentFile() throws Exception {
        String testFile = "src/test/resources/test_input_fail.txt";
        String[] args = new String[2];
        args[0] = "-list";
        args[1] = testFile;

        ColorScanner.main(args);
        assertTrue(outputStreamCaptor.toString().contains("src/test/resources/E101_F8_0112.tif\tICCProfileName:Adobe RGB (1998)\tColorSpace:RGB \t" +
                "InteropIndex:Unknown (R03)\tPhotometricInterpretation:RGB\t\"Dimensions: 2600x3650;Channels: srgb;" +
                "Bit-depth: 16;Alpha channel: False;Color Space: sRGB;Profiles: icc,xmp;" +
                "ICC Profile: Adobe RGB (1998);ICM Profile: ;Dimensions: 114x160;Channels: srgb;Bit-depth: 8;" +
                "Alpha channel: False;Color Space: sRGB;Profiles: ;ICC Profile: ;ICM Profile: ;\""));
        assertTrue(outputStreamCaptor.toString().contains("src/test/resources/P0024_0066.tif\tICCProfileName:null\tColorSpace:null\tInteropIndex:null\t" +
                "PhotometricInterpretation:BlackIsZero\t\"Dimensions: 5300x3841;Channels: gray;Bit-depth: 16;" +
                "Alpha channel: False;Color Space: Gray;Profiles: 8bim,xmp;ICC Profile: ;ICM Profile: ;\""));
        assertTrue(outputStreamCaptor.toString().contains("Error: src/test/resources/test.tif does not exist"));
    }
}
