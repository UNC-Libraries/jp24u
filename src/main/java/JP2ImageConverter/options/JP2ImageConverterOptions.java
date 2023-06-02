package JP2ImageConverter.options;

import picocli.CommandLine.Option;

import java.nio.file.Path;

/**
 * Options for JP2ImageConverter
 * @author krwong
 */
public class JP2ImageConverterOptions {

    @Option(names = {"-f", "--filename"},
            required = true,
            description = "Required. Filename with list of image files to run commands on.")
    private String fileName;

    @Option(names = {"-o", "--output-path"},
            description = "Destination for converted images. You must set the output path manually, no default.")
    private Path outputPath;

    @Option(names = {"-sf", "--source-fmt"},
            description = "Override source file type detection. File extensions (jpeg) and mimetypes (\'image/jpeg\') accepted.",
            defaultValue = "")
    private String sourceFormat;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(Path outputPath) {
        this.outputPath = outputPath;
    }

    public String getSourceFormat() {
        return sourceFormat;
    }

    public void setSourceFormat(String sourceFormat) {
        this.sourceFormat = sourceFormat;
    }
}
