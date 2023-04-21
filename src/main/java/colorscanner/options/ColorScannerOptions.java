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

    @Option(names = {"-o", "--outputPath"},
            description = "Destination for converted images. You must set the output path manually, no default.")
    private String outputPath;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
}
