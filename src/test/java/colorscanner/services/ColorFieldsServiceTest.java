package colorscanner.services;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ColorFieldsServiceTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private ColorFieldsService service;

    @Before
    public void setup() throws Exception {
        System.setOut(new PrintStream(outputStreamCaptor));

        service = new ColorFieldsService();
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

        List<String> fields = service.colorFields(testFile);
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

        List<String> fields = service.colorFields(testFile);
        assertEquals(testFields, fields);
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

        service.allFields(testFile);
        String testOutput = "src/test/resources/P0024_0066.tif\tICCProfileName:null\tColorSpace:null\t" +
                "InteropIndex:null\tPhotometricInterpretation:BlackIsZero\t\"Dimensions: 5300x3841;" +
                "Channels: gray;Bit-depth: 16;Alpha channel: False;Color Space: Gray;Profiles: 8bim,xmp;" +
                "ICC Profile: ;ICM Profile: ;\"\n";
        assertTrue(outputStreamCaptor.toString().contains(testOutput));
    }

//    @Test
//    public void testReadFileInList() throws Exception {
//        String testFile = "src/test/resources/test_input.txt";
//
//        List<String> testListOfImportFiles = new ArrayList<>();
//        testListOfImportFiles.add("src/test/resources/E101_F8_0112.tif");
//        testListOfImportFiles.add("src/test/resources/P0024_0066.tif");
//
//        List<String> listOfImportFiles = service.readFileInList(testFile);
//        assertEquals(testListOfImportFiles, listOfImportFiles);
//    }

    @Test
    public void testListOfImportFiles() throws Exception {
        String testFile = "src/test/resources/test_input.txt";

        service.fileListAllFields(testFile);
        assertTrue(outputStreamCaptor.toString().contains("src/test/resources/E101_F8_0112.tif\t" +
                "ICCProfileName:Adobe RGB (1998)\tColorSpace:RGB \tInteropIndex:Unknown (R03)\t" +
                "PhotometricInterpretation:RGB\t\"Dimensions: 2600x3650;Channels: srgb;" +
                "Bit-depth: 16;Alpha channel: False;Color Space: sRGB;Profiles: icc,xmp;" +
                "ICC Profile: Adobe RGB (1998);ICM Profile: ;Dimensions: 114x160;Channels: srgb;Bit-depth: 8;" +
                "Alpha channel: False;Color Space: sRGB;Profiles: ;ICC Profile: ;ICM Profile: ;\""));
        assertTrue(outputStreamCaptor.toString().contains("src/test/resources/P0024_0066.tif\tICCProfileName:null\t" +
                "ColorSpace:null\tInteropIndex:null\tPhotometricInterpretation:BlackIsZero\t" +
                "\"Dimensions: 5300x3841;Channels: gray;Bit-depth: 16;" +
                "Alpha channel: False;Color Space: Gray;Profiles: 8bim,xmp;ICC Profile: ;ICM Profile: ;\""));
    }

    @Test
    public void testListOfImportFilesWithNonexistentFileFail() throws Exception {
        String testFile = "src/test/resources/test_input_fail.txt";

        try {
            service.fileListAllFields(testFile);
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("src/test/resources/test.tif does not exist. " +
                    "Not processing file list further."));
        }
    }
}
