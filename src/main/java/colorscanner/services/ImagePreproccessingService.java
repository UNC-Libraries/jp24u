package colorscanner.services;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for converting non-tif images and images with unusual color spaces
 * to a temporary image file before kakadu jp2 compression
 * @author krwong
 */
public class ImagePreproccessingService {
    private static final Logger log = getLogger(ImagePreproccessingService.class);

    public static final Path TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));
    public static final Path TMP_FILES_DIR = TMP_DIR.resolve("colorscanner");

    /**
     * For images with CMYK colorspace
     * Run GraphicsMagick convert and convert tif to temp tif
     * @param fileName an image file
     * @return temporaryFile a temporary tif file
     */
    //TODO: we will need to test different CMYK conversion options
    //It seems like only using Color Space creates a more color accurate temporary image.
    //Using Color Space and ICC Profile or just the ICC Profile create a temporary image with slightly different colors.
    public String convertCmykColorSpace(String fileName) throws Exception {
        initializeTempImageFilesDir();

        String gm = "gm";
        String convert = "convert";
        String colorSpace = "-colorspace";
        String colorSpaceOptions = "rgb";
        String profile = "+profile";
        String profileOptions = "\"*\"";
        String temporaryFile = TMP_FILES_DIR.resolve(Paths.get(fileName).getFileName().toString()
                + ".tif").toAbsolutePath().toString();

        List<String> command = Arrays.asList(gm, convert, fileName, colorSpace, colorSpaceOptions,
                profile, profileOptions, temporaryFile);

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
     * Run GraphicsMagick convert and convert other image formats to tiff
     * Other image formats: JPEG, PNG, GIF, PICT, BMP
     * @param fileName an image file
     * @return temporaryFile the path to a temporary tiff file
     */
    //formats accepted by kakadu: TIFF (including BigTIFF), RAW (big-endian), RAWL (little-endian), BMP (they lied), PBM, PGM and PPM
    //formats accepted by metadata-extractor: JPEG, TIFF, WebP, WAV, AVI, PSD, PNG, BMP, GIF, ICO, PCX, QuickTime, MP4, Camera Raw
    public String convertImageFormats(String fileName) throws Exception {
        initializeTempImageFilesDir();

        String gm = "gm";
        String convert = "convert";
        String temporaryFile = TMP_FILES_DIR.resolve(Paths.get(fileName).getFileName().toString()
                + ".tif").toAbsolutePath().toString();

        List<String> command = Arrays.asList(gm, convert, fileName, temporaryFile);

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
     * Run ImageMagick convert and convert JP2 images to tiff
     * GraphicsMagick requires jasper 1.600.0 or later to support JP2
     * @param fileName an image file
     * @return temporaryFile a temporary tiff file
     */
    public String convertJp2(String fileName) throws Exception {
        initializeTempImageFilesDir();

        String convert = "convert";
        String importFile = fileName;
        String temporaryFile = TMP_FILES_DIR.resolve(Paths.get(fileName).getFileName().toString()
                + ".tif").toAbsolutePath().toString();

        List<String> command = Arrays.asList(convert, importFile, temporaryFile);

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
     * Determine image format and preprocess if needed
     * for non-TIFF image formats: convert to temp tiff before kdu_compress
     * currently supported image formats: TIFF, JPEG, PNG, GIF, PICT, BMP, PSD
     * @param fileName an image file
     * @return inputFile a path to a TIFF image file
     */
    public String convertToTiff(String fileName) throws Exception {
        String inputFile;
        String fileNameExtension = FilenameUtils.getExtension(fileName).toLowerCase();
        Set<String> imageFormats = new HashSet<>(Arrays.asList("jpeg", "jpg", "png", "gif", "pict", "pct", "pic", "bmp"));

        if (imageFormats.contains(fileNameExtension)) {
            inputFile = convertImageFormats(fileName);
        } else if (fileNameExtension.matches("psd")) {
            inputFile = convertPsd(fileName);
        } else if (fileNameExtension.matches("jp2")) {
            inputFile = convertJp2(fileName);
        } else if (fileNameExtension.matches("tiff") || fileNameExtension.matches("tif")){
            inputFile = fileName;
        } else {
            log.info("JP2 conversion for the following file format not supported: {}", fileNameExtension);
            throw new Exception("JP2 conversion for the following file format not supported: " + fileNameExtension);
        }

        return inputFile;
    }

    /**
     * Determine image colorspace and preprocess if needed
     * for unusual colorspaces: convert to temp TIFF and set colorspace to RGB before kdu_compress
     * currently supported colorspaces: RGB, sRGB, RGB Palette, Gray, CMYK
     * @param fileName an image file
     * @return inputFile a path to a TIFF image file
     */
    public String convertColorSpaces(String colorSpace, String fileName) throws Exception {
        String inputFile;
        Set<String> colorSpaces = new HashSet<>(Arrays.asList("rgb", "srgb", "rgb palette", "gray"));

        if (colorSpace.toLowerCase().contains("cmyk")) {
            inputFile = convertCmykColorSpace(fileName);
        } else if (colorSpaces.contains(colorSpace.toLowerCase())) {
            inputFile = fileName;
        } else {
            log.info("JP2 conversion for the following colorspace not supported: {}", colorSpace);
            throw new Exception("JP2 conversion for the following colorspace not supported: " + colorSpace);
        }

        return inputFile;
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