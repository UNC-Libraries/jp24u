package colorscanner.options;

import picocli.CommandLine.Option;

/**
 * Options for color scanner
 * @author krwong
 */
public class ColorScannerOptions {

    @Option(names = {"-f", "--filename"},
            required = true,
            description = "Required. Filename with list of image files to run commands on.")
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
