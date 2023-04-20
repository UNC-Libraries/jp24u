package colorscanner.services;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for converting a CMYK source image to a temporary image file before kakadu jp2 compression
 * @author krwong
 */
public class TemporaryImageService {
    private static final Logger log = getLogger(TemporaryImageService.class);

    public static final Path TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));
    public static final Path TMP_FILES_DIR = TMP_DIR.resolve("colorscanner");

    /**
     * Run ImageMagick convert and convert tif to jpg
     * @param fileName an image file
     * @return temporaryFile a temporary jpg file
     */
    //TODO: we will need to test different CMYK conversion options
    //It seems like only using Color Space creates a more color accurate temporary image.
    //Using Color Space and ICC Profile or just the ICC Profile create a temporary image with slightly different colors.
    public String convertImage(String fileName) throws Exception {
        initializeTempImageFilesDir();

        String convert = "convert";
        //String profile = "-profile";
        //String profileOptions = "src/main/resources/AdobeRGB1998.icc";
        String colorSpace = "-colorspace";
        String colorSpaceOptions = "srgb";
        String temporaryFile = TMP_FILES_DIR.resolve(Paths.get(fileName).getFileName().toString()
                + ".jpg").toAbsolutePath().toString();

        List<String> command = Arrays.asList(convert, fileName, colorSpace, colorSpaceOptions,
                temporaryFile);

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String cmdOutput = new String(process.getInputStream().readAllBytes());
            log.info(cmdOutput);
        } catch (Exception e) {
            throw new Exception(fileName + " failed to generate jpg file.", e);
        }

        return temporaryFile;
    }

    /**
     * Run ImageMagick convert and convert PSD images to tiff
     * GraphicsMagick doesn't support PSD
     * @param fileName an image file
     * @return temporaryFile a temporary tiff file
     */
    public String convertPsd(String fileName) throws Exception {
        initializeTempImageFilesDir();

        String convert = "convert";
        String importFile = fileName + "[0]";
        //if converting the [0] flattened layer doesn't work, try removing the [0] and adding -flatten to the command
        //String importFile = fileName;
        //String flatten = "-flatten";
        String colorspace = "-colorspace";
        String colorspaceOptions = "sRGB";
        String temporaryFile = TMP_FILES_DIR.resolve(Paths.get(fileName).getFileName().toString()
                + ".tif").toAbsolutePath().toString();

        List<String> command = Arrays.asList(convert, importFile, colorspace, colorspaceOptions, temporaryFile);

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String cmdOutput = new String(process.getInputStream().readAllBytes());
            log.info(cmdOutput);
        } catch (Exception e) {
            throw new Exception(fileName + " failed to generate tiff file.", e);
        }

        return temporaryFile;
    }

    /**
     * Create tmp image files directory for jpgs
     * @return tmpImageFilesDirectoryPath
     */
    public Path initializeTempImageFilesDir() throws IOException {
        Path path = TMP_FILES_DIR;
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    /**
     * Delete tmp image files directory
     */
    public void deleteTmpImageFilesDir() throws Exception {
        File tmpDir = new File(TMP_FILES_DIR.toString());
        FileUtils.deleteDirectory(tmpDir);
        tmpDir.delete();
    }
}
