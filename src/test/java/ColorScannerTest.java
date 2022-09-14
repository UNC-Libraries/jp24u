/**
 * Copyright 2008 The University of North Carolina at Chapel Hill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
    public void testFileSize() throws Exception {
        String testFile = "src/test/resources/lorem_ipsum.txt";
        String[] args = new String[1];
        args[0] = testFile;

        colorScanner.main(args);

        assertEquals("File size: 3278", outputStreamCaptor.toString().trim());
    }

    @Test
    public void testMultipleArgumentsFail() throws Exception {
        String[] args = new String[2];
        args[0] = "src/test/resources/lorem_ipsum.txt";
        args[1] = "test";

        try {
            ColorScanner.main(args);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Error: Only accepts one filename."));
        }
    }

    @Test
    public void testNoFileArg() throws Exception {
        String[] args = new String[1];
        args[0] = "test";
        ColorScanner.main(args);

        try {
            ColorScanner.main(args);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Error: Only accepts one filename"));
        }
    }

}
