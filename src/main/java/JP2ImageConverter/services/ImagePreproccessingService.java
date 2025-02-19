package JP2ImageConverter.services;

import JP2ImageConverter.util.CommandUtility;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for converting non-TIFF images and images with unusual color spaces
 * to a temporary image file before Kakadu JP2 compression
 * @author krwong
 */
public class ImagePreproccessingService {
    private static final Logger log = getLogger(ImagePreproccessingService.class);

    private static final String GM = "gm";
    private static final String CONVERT = "convert";
    private static final String DCRAW = "dcraw";
    private static final String EXIFTOOL = "exiftool";

    public Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
    public Path tmpFilesDir = tmpDir.resolve("JP2ImageConverter");
    private static final String AUTO_ORIENT = "-auto-orient";

    public ImagePreproccessingService() {
        try {
            initializeTempImageFilesDir();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * For images with unusual colorspaces and unsupported ICC Profiles
     * Run GraphicsMagick convert and convert TIFF to temporary TIFF
     * @param fileName an image file
     * @return temporaryFile a temporary TIFF file
     */
    //It seems like only using color space creates a more color accurate temporary image.
    //Using color space and ICC Profile or just the ICC Profile create a temporary image with slightly different colors.
    public String setColorSpaceRemoveProfileWithIm(String fileName) throws Exception {
        String colorSpace = "-colorspace";
        String colorSpaceOptions = "rgb";
        String profile = "+profile";
        String profileOptions = "\"*\"";
        String temporaryFile = String.valueOf(prepareTempPath(fileName, ".tif"));

        List<String> command = Arrays.asList(GM, CONVERT, AUTO_ORIENT, fileName, colorSpace, colorSpaceOptions,
                profile, profileOptions, temporaryFile);
        CommandUtility.executeCommand(command);

        return temporaryFile;
    }

    /**
     * For images with CIELab color space
     * Run ImageMagick convert and convert TIFF to temporary TIFF (GraphicsMagick and dcraw don't support CIELab)
     * @param fileName an image file
     * @return temporaryFile a temporary TIFF file
     */
    public String setColorSpaceWithIm(String fileName) throws Exception {
        String temporaryFile = prepareTempPath(fileName, ".tif").toString();
        String colorSpace = "-colorspace";
        String colorSpaceOptions = "sRGB";

        List<String> command = Arrays.asList(CONVERT, AUTO_ORIENT, fileName, colorSpace, colorSpaceOptions,
                temporaryFile);
        CommandUtility.executeCommand(command);

        return temporaryFile;
    }

    /**
     * Run GraphicsMagick convert and convert other image formats/raw image formats to TIFF
     * Other image formats: PNG, GIF, PICT, BMP
     * Raw image formats: CRW, DNG, RAF
     * @param fileName an image file
     * @return temporaryFile the path to a temporary TIFF file
     */
    // formats accepted by kakadu: TIFF (including BigTIFF), RAW (big-endian), RAWL (little-endian), BMP (they lied), PBM, PGM and PPM
    // formats accepted by metadata-extractor: JPEG, TIFF, WebP, WAV, AVI, PSD, PNG, BMP, GIF, ICO, PCX, QuickTime, MP4, Camera Raw
    public String convertToTifWithGm(String fileName) throws Exception {
        String inputFile = fileName + "[0]";
        String temporaryFile = String.valueOf(prepareTempPath(fileName, ".tif"));

        List<String> command = Arrays.asList(GM, CONVERT, AUTO_ORIENT, inputFile, temporaryFile);
        CommandUtility.executeCommand(command);

        return temporaryFile;
    }

    /**
     * Run ImageMagick convert and convert PSD images to TIFF
     * GraphicsMagick doesn't support PSD
     * @param fileName an image file
     * @return temporaryFile a temporary TIFF file
     */
    public String flattenSetColorspaceConvertToTifWithIm(String fileName) throws Exception {
        String importFile = fileName + "[0]";
        // if converting the [0] flattened layer doesn't work, try removing the [0] and adding -flatten to the command
        // String importFile = fileName;
        // String flatten = "-flatten";
        String colorspace = "-colorspace";
        String colorspaceOptions = "sRGB";
        String temporaryFile = String.valueOf(prepareTempPath(fileName, ".tif"));

        List<String> command = Arrays.asList(CONVERT, AUTO_ORIENT, importFile, colorspace, colorspaceOptions, temporaryFile);
        CommandUtility.executeCommand(command);

        return temporaryFile;
    }

    /**
     * Run ImageMagick convert and convert JP2 images to TIFF
     * GraphicsMagick requires jasper 1.600.0 or later to support JP2
     * @param fileName an image file
     * @return temporaryFile a temporary TIFF file
     */
    public String convertToTifWithIm(String fileName) throws Exception {
        String importFile = fileName;
        String temporaryFile = String.valueOf(prepareTempPath(fileName, ".tif"));

        List<String> command = Arrays.asList(CONVERT, AUTO_ORIENT, importFile, temporaryFile);
        CommandUtility.executeCommand(command);

        return temporaryFile;
    }

    /**
     * Run ImageMagick convert and convert JPEG images to PPM
     * Converting JPEG to temporary TIFFs results in Kakadu errors and 0 byte JP2s
     * @param fileName an image file
     * @return temporaryFile a temporary PPM file
     */
    public String convertToPpmWithIm(String fileName) throws Exception {
        String importFile = fileName;
        String temporaryFile = String.valueOf(prepareTempPath(fileName, ".ppm"));

        List<String> command = Arrays.asList(CONVERT, AUTO_ORIENT, importFile, temporaryFile);
        CommandUtility.executeCommand(command);

        return temporaryFile;
    }

    /**
     * Run GraphicsMagick convert and convert CR2 images to PPM
     * Converting CR2 to temporary TIFFs results in YCrCb colorspaces
     * @param fileName an image file
     * @return temporaryFile a temporary PPM file
     */
    public String convertToPpmWithGm(String fileName) throws Exception {
        String importFile = fileName;
        String temporaryFile = String.valueOf(prepareTempPath(fileName, ".ppm"));

        List<String> command = Arrays.asList(GM, CONVERT, AUTO_ORIENT, importFile, temporaryFile);
        CommandUtility.executeCommand(command);

        return temporaryFile;
    }

    /**
     * Run Exiftool to convert NEF and NRW images to JPEG
     * @param fileName an NEF or NRW image file
     * @return temporaryFile a temporary JPEG file
     */
    public String convertToJpgWithExiftool(String fileName) throws Exception {
        String b = "-b";
        String jpgFromRaw = "-JpgFromRaw";
        String inputFile = fileName;
        String temporaryFile = String.valueOf(prepareTempPath(fileName, ".jpeg"));

        List<String> command = Arrays.asList(EXIFTOOL, b, jpgFromRaw, inputFile);
        CommandUtility.executeCommandWriteToFile(command, temporaryFile);

        // Next, copy over orientation info from the original NEF/NRW to the new JPEG
        List<String> command2 = Arrays.asList(EXIFTOOL, "-overwrite_original", "-tagsfromfile", inputFile, "-orientation", temporaryFile);
        CommandUtility.executeCommand(command2);

        return temporaryFile;
    }

    /**
     * Convert a PCD image to a temporary TIFF
     * @param fileName filename of the pcd file
     * @return a temporary JPEG file
     * @throws Exception
     */
    public String convertToTifHighestResolutionWithGm(String fileName) throws Exception {
        // 6 is the index of the highest resolution for this format
        String inputFile = fileName + "[6]";
        String temporaryFile = String.valueOf(prepareTempPath(fileName, ".tif"));

        List<String> command = Arrays.asList(GM, CONVERT, AUTO_ORIENT, inputFile, temporaryFile);
        CommandUtility.executeCommand(command);

        return temporaryFile;
    }

    /**
     * Convert a RW2 image to a temporary PPM
     * @param fileName filename of the rw2 file
     * @return a temporary PPM file
     * @throws Exception
     */
    public String convertToPpmWithDcraw(String fileName) throws Exception {
        String temporaryFile = prepareTempPath(fileName, ".ppm").toString();
        List<String> dcrawCommand = Arrays.asList(DCRAW, "-c", "-w", fileName);
        CommandUtility.executeCommandWriteToFile(dcrawCommand, temporaryFile);

        return temporaryFile;
    }

    /**
     * Removes the alpha channel from the provided image
     * @param fileName
     * @return
     * @throws Exception
     */
    public String removeAlphaChannel(String fileName) throws Exception {
        String temporaryFile = String.valueOf(prepareTempPath(fileName, ".tif"));

        List<String> command = Arrays.asList(CONVERT, AUTO_ORIENT, "-alpha", "off", fileName, temporaryFile);
        CommandUtility.executeCommand(command);

        return temporaryFile;
    }

    /**
     * Using GraphicsMagick, set the type to TrueColor and colorspace to sRGB for the provided image
     * For images with type "Palette" and colorspace "RGB Palette"
     * @param fileName
     * @return
     * @throws Exception
     */
    public String setTypeAndColorspaceWithGm(String fileName) throws Exception {
        String temporaryFile = String.valueOf(prepareTempPath(fileName, ".tif"));

        List<String> command = Arrays.asList(GM, CONVERT, AUTO_ORIENT, "-type", "TrueColor",
                "-colorspace", "sRGB", fileName,temporaryFile);
        CommandUtility.executeCommand(command);

        return temporaryFile;
    }

    /**
     * Determine image format and preprocess if needed
     * for non-TIFF image formats: convert to temporary TIFF/PPM before kdu_compress
     * currently supported image formats: TIFF, JPEG, PNG, GIF, PICT, BMP, PSD, NEF, NRW, CRW, CR2, DNG, RAF, PCD, RW2
     * @param fileName an image file
     * @param sourceFormat file extension/mimetype override
     * @return inputFile a path to a TIFF/PPM image file
     */
    public String convertToTiff(String fileName, String sourceFormat) throws Exception {
        String inputFile;
        String fileNameExtension = FilenameUtils.getExtension(fileName).toLowerCase();
        Set<String> imageFormats = new HashSet<>(Arrays.asList("png", "gif", "pct", "bmp", "crw", "raf", "dng"));
        if (!sourceFormat.isEmpty()) {
            fileNameExtension = sourceFormat;
        }

        if (imageFormats.contains(fileNameExtension)) {
            inputFile = convertToTifWithGm(fileName);
        } else if (fileNameExtension.matches("psd")) {
            inputFile = flattenSetColorspaceConvertToTifWithIm(fileName);
        } else if (fileNameExtension.matches("jp2")) {
            inputFile = convertToTifWithIm(fileName);
        } else if (fileNameExtension.matches("jpeg")) {
            inputFile = convertToPpmWithIm(fileName);
        } else if (fileNameExtension.matches("cr2")) {
            inputFile = convertToPpmWithGm(fileName);
        } else if (fileNameExtension.matches("pcd")) {
            inputFile = convertToTifHighestResolutionWithGm(fileName);
        } else if (fileNameExtension.matches("rw2")) {
            inputFile = convertToPpmWithDcraw(fileName);
        } else if (fileNameExtension.matches("nef") || fileNameExtension.matches("nrw")) {
            // convert NEF/NRW to JPEG, then convert JPEG to PPM
            String tempJpeg = convertToJpgWithExiftool(fileName);
            inputFile = convertToPpmWithIm(tempJpeg);
            // delete temp JPEG after temp PPM is created
            Files.deleteIfExists(Path.of(tempJpeg));
        } else if (fileNameExtension.matches("tiff") || fileNameExtension.matches("tif")) {
            inputFile = linkToTiff(fileName);
        } else {
            log.info("JP2 conversion for the following file format not supported: {}", fileNameExtension);
            throw new Exception("JP2 conversion for the following file format not supported: " + fileNameExtension);
        }

        return inputFile;
    }

    /**
     * Determine image color space and preprocess if needed
     * for unusual color spaces: convert to temporary TIFF and set color space to RGB before kdu_compress
     * currently supported color spaces: RGB, sRGB, RGB Palette, Gray, CMYK, AToB0 (technically an ICC Profile)
     * @param colorSpace an image color space
     * @param type an image type
     * @param fileName an image file
     * @return inputFile a path to a TIFF image file
     */
    public String convertColorSpaces(String colorSpace, String type, String fileName) throws Exception {
        String inputFile;
        Set<String> colorSpaces = new HashSet<>(Arrays.asList("rgb", "srgb", "rgb palette", "gray"));
        Set<String> unusualColorSpaces = new HashSet<>(Arrays.asList("cmyk", "ycbcr", "atob0", "color filter array"));

        if (colorSpace.toLowerCase().contains("rgb palette") && type.toLowerCase().contains("palette")) {
            inputFile = setTypeAndColorspaceWithGm(fileName);
        } else if (colorSpace.toLowerCase().contains("cielab")) {
            inputFile = setColorSpaceWithIm(fileName);
        } else if (colorSpace.toLowerCase().contains("ycbcr") && FilenameUtils.isExtension(fileName, "tif")) {
            // rarely, Kakadu can't parse a TIFF with a YCbCr photometric interpretation even after correction
            // convert the TIFF to a PPM before JP2 generation
            inputFile = convertToPpmWithGm(fileName);
        } else if (unusualColorSpaces.contains(colorSpace.toLowerCase())) {
            inputFile = setColorSpaceRemoveProfileWithIm(fileName);
        } else if (colorSpaces.contains(colorSpace.toLowerCase())) {
            inputFile = fileName;
        } else {
            log.info("JP2 conversion for the following color space not supported: {}", colorSpace);
            throw new Exception("JP2 conversion for the following color space not supported: " + colorSpace);
        }

        return inputFile;
    }

    /**
     * Create a temporary image file with correct orientation
     * @param fileName an image file
     * @return path to a temporary image file
     * @throws Exception
     */
    public String correctOrientation(String fileName) throws Exception {
        String extension = FilenameUtils.getExtension(fileName);
        String temporaryFile = String.valueOf(prepareTempPath(fileName, "." + extension));

        List<String> command = Arrays.asList(GM, CONVERT, AUTO_ORIENT, fileName, temporaryFile);
        CommandUtility.executeCommand(command);

        return temporaryFile;
    }

    public String handleIccProfile(String fileName) throws Exception {
        // For now, we will just strip out the ICC Profile
        var temporaryFile = prepareTempPath(fileName, ".tif").toString();
        var clearProfileCommand = Arrays.asList(EXIFTOOL, "-icc_profile=", fileName, "-o", temporaryFile);
        CommandUtility.executeCommand(clearProfileCommand);
        return temporaryFile;
    }

    /**
     * Create symbolic link for TIFF
     * @param fileName an image file
     * @return link a path to a TIFF image file
     */
    public String linkToTiff(String fileName) throws Exception {
        Path target = Paths.get(fileName).toAbsolutePath();
        Path link = prepareTempPath(fileName, ".tif");
        Files.createSymbolicLink(link, target);

        return link.toAbsolutePath().toString();
    }

    /**
     * Create tmp image files directory for temporary files
     * @return tmpImageFilesDirectoryPath
     */
    public Path initializeTempImageFilesDir() throws Exception {
        Path path = tmpFilesDir;
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    /**
     * Create temporary image file path and delete temporary file if it already exists
     * @return tmpImageFilesDirectoryPath
     */
    private Path prepareTempPath(String fileName, String extension) throws Exception {
        Path tempPath = Files.createTempFile(tmpFilesDir, FilenameUtils.getName(fileName), extension);
        // delete temporary path so that it can be written over by whatever utility has requested a path
        Files.delete(tempPath);
        return tempPath;
    }
}
