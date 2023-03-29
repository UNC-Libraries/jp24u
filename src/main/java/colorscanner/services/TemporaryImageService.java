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
 * Service for converting non-tif images and images with unusual color spaces
 * to a temporary image file before kakadu jp2 compression
 * @author krwong
 */
public class TemporaryImageService {
    private static final Logger log = getLogger(TemporaryImageService.class);

    public static final Path TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));
    public static final Path TMP_FILES_DIR = TMP_DIR.resolve("colorscanner");

    /**
     * For images with CMYK colorspace
     * Run ImageMagick convert and convert tif to temp tif
     * @param fileName an image file
     * @return temporaryFile a temporary tif file
     */
    //TODO: we will need to test different CMYK conversion options
    //It seems like only using Color Space creates a more color accurate temporary image.
    //Using Color Space and ICC Profile or just the ICC Profile create a temporary image with slightly different colors.
    public String convertCmykColorSpace(String fileName) throws Exception {
        initializeTempImageFilesDir();

        String convert = "convert";
//        String profile = "-profile";
//        String profileOptions = "src/main/resources/AdobeRGB1998.icc";
        String colorSpace = "-colorspace";
        String colorSpaceOptions = "srgb";
        String temporaryFile = TMP_FILES_DIR.resolve(Paths.get(fileName).getFileName().toString()
                + ".tif").toAbsolutePath().toString();

        List<String> command = Arrays.asList(convert, fileName, colorSpace, colorSpaceOptions,
                temporaryFile);

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String cmdOutput = new String(process.getInputStream().readAllBytes());
            log.info(cmdOutput);
        } catch (Exception e) {
            throw new Exception(fileName + " failed to generate temp tif file.", e);
        }

        return temporaryFile;
    }

    /**
     * Run ImageMagick convert and convert other image formats to tiff
     * Other image formats: JPEG, PNG, GIF, PICT, BMP
     * @param fileName an image file
     * @return temporaryFile a temporary tiff file
     */
    //formats accepted by kakadu: TIFF (including BigTIFF), RAW (big-endian), RAWL (little-endian), BMP, PBM, PGM and PPM
    //formats accepted by metadata-extractor: JPEG, TIFF, WebP, WAV, AVI, PSD, PNG, BMP, GIF, ICO, PCX, QuickTime, MP4, Camera Raw
    public String convertImageFormats(String fileName) throws Exception {
        initializeTempImageFilesDir();

        String convert = "convert";
        //String temporaryFile = fileName + ".tif";
        String temporaryFile = TMP_FILES_DIR.resolve(Paths.get(fileName).getFileName().toString()
                + ".tif").toAbsolutePath().toString();

        List<String> command = Arrays.asList(convert, fileName, temporaryFile);

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
