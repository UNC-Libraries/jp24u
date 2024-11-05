package JP2ImageConverter.services;

import JP2ImageConverter.errors.CommandException;
import JP2ImageConverter.util.CommandUtility;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for Kakadu kduCompress
 * Supported image formats: TIFF, JPEG, PNG, GIF, PICT, BMP, PSD, JP2, NEF, CRW, CR2, DNG, RAF, PCD
 * @author krwong
 */
public class KakaduService {
    private static final Logger log = getLogger(KakaduService.class);
    private final static Map<String, String> SOURCE_FORMATS = new HashMap<>();
    // accepted file types are listed in sourceFormats below
    static {
        SOURCE_FORMATS.put("tiff", "tiff");
        SOURCE_FORMATS.put("tif", "tiff");
        SOURCE_FORMATS.put("image/tiff", "tiff");
        SOURCE_FORMATS.put("jpeg", "jpeg");
        SOURCE_FORMATS.put("jpg", "jpeg");
        SOURCE_FORMATS.put("image/jpeg", "jpeg");
        SOURCE_FORMATS.put("png", "png");
        SOURCE_FORMATS.put("image/png", "png");
        SOURCE_FORMATS.put("gif", "gif");
        SOURCE_FORMATS.put("image/gif", "gif");
        SOURCE_FORMATS.put("pict", "pct");
        SOURCE_FORMATS.put("pct", "pct");
        SOURCE_FORMATS.put("pic", "pct");
        SOURCE_FORMATS.put("image/x-pict", "pct");
        SOURCE_FORMATS.put("bmp", "bmp");
        SOURCE_FORMATS.put("image/bmp", "bmp");
        SOURCE_FORMATS.put("psd", "psd");
        SOURCE_FORMATS.put("image/psd", "psd");
        SOURCE_FORMATS.put("image/vnd.adobe.photoshop", "psd");
        SOURCE_FORMATS.put("jp2", "jp2");
        SOURCE_FORMATS.put("image/jp2", "jp2");
        SOURCE_FORMATS.put("nef", "nef");
        SOURCE_FORMATS.put("image/x-nikon-nef", "nef");
        SOURCE_FORMATS.put("crw", "crw");
        SOURCE_FORMATS.put("image/x-canon-crw", "crw");
        SOURCE_FORMATS.put("cr2", "cr2");
        SOURCE_FORMATS.put("image/x-canon-cr2", "cr2");
        SOURCE_FORMATS.put("dng", "dng");
        SOURCE_FORMATS.put("image/x-adobe-dng", "dng");
        SOURCE_FORMATS.put("raf", "raf");
        SOURCE_FORMATS.put("image/x-fujifilm-raf", "raf");
        SOURCE_FORMATS.put("pcd", "pcd");
        SOURCE_FORMATS.put("image/x-photo-cd", "pcd");
        SOURCE_FORMATS.put("rw2", "rw2");
        SOURCE_FORMATS.put("image/x-panasonic-rw2", "rw2");
    }

    public Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));

    private ColorFieldsService colorFieldsService;
    private ImagePreproccessingService imagePreproccessingService;

    /**
     * Get color space from EXIF fields
     * @param preprocessedImageMetadata Extracted metadata from the preprocessed image
     * @param originalImageMetadata Extracted metadata from the original image
     * @param originalImage the original input image
     * @return colorSpace
     */
    public String getColorSpace(Map<String, String> preprocessedImageMetadata,
                                Map<String, String> originalImageMetadata,
                                String originalImage) {
        String colorSpace;
        var imageType = colorFieldsService.identifyType(originalImage);

        // Identify image type for grayscale images and set color space to gray.
        // Check 2 EXIF fields (ColorSpace and PhotometricInterpretation) for color space information.
        // If the preprocessed image does not have a color space, check the original image for color space information.
        // If no color space is found with metadata-extractor, set color space to sRGB.
        if (imageType != null && imageType.contains("Grayscale")) {
            colorSpace = "Gray";
        } else if (preprocessedImageMetadata.get(ColorFieldsService.COLOR_SPACE) != null) {
            colorSpace = preprocessedImageMetadata.get(ColorFieldsService.COLOR_SPACE);
        } else if (preprocessedImageMetadata.get(ColorFieldsService.PHOTOMETRIC_INTERPRETATION) != null) {
            colorSpace = preprocessedImageMetadata.get(ColorFieldsService.PHOTOMETRIC_INTERPRETATION);
        } else if (originalImageMetadata.get(ColorFieldsService.COLOR_SPACE) != null) {
            colorSpace = originalImageMetadata.get(ColorFieldsService.COLOR_SPACE);
        } else if (originalImageMetadata.get(ColorFieldsService.PHOTOMETRIC_INTERPRETATION) != null &&
                !originalImageMetadata.get(ColorFieldsService.PHOTOMETRIC_INTERPRETATION).equalsIgnoreCase("ycbcr")) {
            colorSpace = originalImageMetadata.get(ColorFieldsService.PHOTOMETRIC_INTERPRETATION);
        } else {
            colorSpace = "sRGB";
        }

        // if ColorSpace is BlackIsZero or WhiteIsZero, it is a grayscale image
        if (colorSpace.toLowerCase().matches("blackiszero") ||
                colorSpace.toLowerCase().matches("whiteiszero")) {
            colorSpace = "Gray";
        }

        // If the original image has the ICCProfile AToB0, set the color space to aToB0
        if (originalImageMetadata.get(ColorFieldsService.A_TO_B0) != null) {
            colorSpace = "aToB0";
        }

        return colorSpace;
    }

    /**
     * Extract metadata from files of formats supported by metadata-extractor.
     * Return an empty collection for unsupported types (JP2, PICT, PPM)
     * @param fileName an image file
     * @param sourceFormat file extension/mimetype override
     * @return imageMetadata
     */
    protected Map<String, String> extractMetadata(String fileName, String sourceFormat) throws Exception {
        String extension = sourceFormat.isEmpty() ? FilenameUtils.getExtension(fileName).toLowerCase() : sourceFormat;
        if (extension.equals("jp2") || extension.equals("pct") || extension.equals("ppm")) {
            return Collections.emptyMap();
        }
        return colorFieldsService.extractMetadataFields(fileName);
    }

    /**
     * Run kdu_compress and convert image to JP2
     * @param sourceFileName an image file
     * @param outputPath destination for converted files
     * @param sourceFormat file extension/mimetype override
     */
    public void kduCompress(String sourceFileName, Path outputPath, String sourceFormat) throws Exception {
        // list of intermediate files to delete after JP2 is created
        List<String> intermediateFiles = new ArrayList<>();

        try {
            // override source file type detection with user-inputted image file type
            String fileName = sourceFileName;
            sourceFormat = getSourceFormat(fileName, sourceFormat);

            // Create a symlink to the original file in order to add a file extension
            fileName = linkToOriginal(fileName, sourceFormat, intermediateFiles);

            String kduCompress = "kdu_compress";
            String input = "-i";
            // preprocess non-TIFF images and convert them to temporary TIFFs before kdu_compress
            String inputFile = imagePreproccessingService.convertToTiff(fileName, sourceFormat);
            intermediateFiles.add(inputFile);
            String output = "-o";
            String outputFile;
            String outputDefaultFilename = FilenameUtils.getBaseName(fileName) + ".jp2";

            // if the output path is a directory
            if (Files.isDirectory(outputPath)) {
                outputFile = outputPath + "/" + outputDefaultFilename;
                // if the output path is a file
            } else if (Files.exists(outputPath.getParent())) {
                outputFile = outputPath + ".jp2";
            } else {
                throw new Exception(outputPath + " does not exist.");
            }

            String clevels = "Clevels=6";
            String clayers = "Clayers=6";
            String cprecincts = "Cprecincts={256,256},{256,256},{128,128}";
            String stiles = "Stiles={512,512}";
            String corder = "Corder=RPCL";
            String orggenplt = "ORGgen_plt=yes";
            String orgtparts = "ORGtparts=R";
            String cblk = "Cblk={64,64}";
            String cusesop = "Cuse_sop=yes";
            String cuseeph = "Cuse_eph=yes";
            String flushPeriod = "-flush_period";
            String flushPeriodOptions = "1024";
            String rate = "-rate";
            String rateOptions = "3";
            String weights = "-no_weights";
            String jp2Space;
            String jp2SpaceOptions;
            String noPalette;

            // Perform corrections to the input image
            var originalImageMetadata = extractMetadata(fileName, sourceFormat);
            var preprocessedImageMetadata = originalImageMetadata;
            if (!fileName.equals(inputFile)) {
                preprocessedImageMetadata = extractMetadata(inputFile, "");
            }
            String colorSpace = getColorSpace(preprocessedImageMetadata, originalImageMetadata, fileName);
            String orientation = originalImageMetadata.get(ColorFieldsService.ORIENTATION);
            inputFile = correctInputImage(inputFile, fileName, sourceFormat, colorSpace, orientation, intermediateFiles);

            List<String> command = new ArrayList<>(Arrays.asList(kduCompress, input, inputFile, output, outputFile,
                    clevels, clayers, cprecincts, stiles, corder, orggenplt, orgtparts, cblk, cusesop, cuseeph,
                    flushPeriod, flushPeriodOptions, rate, rateOptions, weights));

            // for GIF images: add no_palette to command
            if (FilenameUtils.getExtension(fileName).equalsIgnoreCase("gif") || sourceFormat.equals("gif")) {
                noPalette = "-no_palette";
                command.add(noPalette);
            }

            // for grayscale images: add jp2Space to command
            if (colorSpace.equalsIgnoreCase("gray")) {
                jp2Space = "-jp2_space";
                jp2SpaceOptions = "sLUM";
                command.add(jp2Space);
                command.add(jp2SpaceOptions);
            }

            performKakaduCommandWithRecovery(command, intermediateFiles, true);
            deleteTinyGrayVoidImages(outputFile);
        } finally {
            // delete intermediate files and symlinks after JP2 generated
            for (String intermediateFile : intermediateFiles) {
                Files.deleteIfExists(Path.of(intermediateFile));
            }
        }
    }

    private void performKakaduCommandWithRecovery(List<String> command, List<String> intermediateFiles, boolean retry) throws Exception {
        try {
            log.debug("Performing kakadu command: {}", command);
            CommandUtility.executeCommand(command);
        } catch (CommandException e) {
            var output = e.getOutput();
            if (retry) {
                if (output.contains("ICC profile") && output.contains("reproduction curve appears to have been truncated")) {
                    log.warn("Invalid ICC profile, retrying without ICC profile: {}", e.getMessage());
                    var inputIndex = command.indexOf("-i") + 1;
                    var modifiedTmpPath = imagePreproccessingService.handleIccProfile(command.get(inputIndex));
                    command.set(inputIndex, modifiedTmpPath);
                    intermediateFiles.add(modifiedTmpPath);
                    performKakaduCommandWithRecovery(command, intermediateFiles, false);
                    return;
                }
            }
            throw e;
        }
    }

    private String getSourceFormat(String fileName, String sourceFormat) {
        String format;
        if (sourceFormat == null || sourceFormat.isEmpty()) {
            format = FilenameUtils.getExtension(fileName).toLowerCase();
        } else {
            format = sourceFormat;
        }
        if (format == null || format.isEmpty()) {
            throw new IllegalArgumentException("Source format could not be determined for " + fileName);
        }
        if (!SOURCE_FORMATS.containsKey(format)) {
            throw new IllegalArgumentException("JP2 conversion for the following file format not supported: " + format);
        }
        return SOURCE_FORMATS.get(format);
    }

    /**
     * Performs corrections on the input image, such as fixing the color space and orientation
     * @param inputFile
     * @param fileName
     * @param sourceFormat
     * @param colorSpace
     * @param orientation
     * @param intermediateFiles
     * @return
     * @throws Exception
     */
    private String correctInputImage(String inputFile, String fileName, String sourceFormat, String colorSpace,
                                     String orientation, List<String> intermediateFiles) throws Exception {
        var fileBeforeColorConversion = inputFile;
        // for unusual color spaces (CMYK and YcbCr): convert to temporary TIFF before kduCompress
        inputFile = imagePreproccessingService.convertColorSpaces(colorSpace, inputFile);
        if (fileBeforeColorConversion.equals(inputFile)) {
            // Create a temporary TIFF with the correct orientation if no color space conversion was done
            // and the orientation is different from the default.
            var format = sourceFormat != null && !sourceFormat.isEmpty() ?
                    sourceFormat : SOURCE_FORMATS.get(FilenameUtils.getExtension(fileName));
            if (orientation != null && format != null && format.equals("tiff")
                    && !ColorFieldsService.ORIENTATION_DEFAULT.equals(orientation)) {
                inputFile = imagePreproccessingService.correctOrientation(fileName);
                intermediateFiles.add(inputFile);
            }
        } else {
            intermediateFiles.add(inputFile);
        }
        return inputFile;
    }

    /**
     * Iterate through list of image files and run kdu_compress to convert all images to JP2s
     * @param fileName a list of image files
     * @param outputPath destination for converted files
     * @param sourceFormat file extension/mimetype override
     */
    public void fileListKduCompress(String fileName, Path outputPath, String sourceFormat) throws Exception {
        List<String> listOfFiles = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);

        Iterator<String> itr = listOfFiles.iterator();
        while (itr.hasNext()) {
            String imageFileName = itr.next();
            if (Files.exists(Paths.get(imageFileName))) {
                kduCompress(imageFileName, outputPath, sourceFormat);
            } else {
                throw new Exception(imageFileName + " does not exist. Not processing file list further.");
            }
        }
    }

    /**
     * After JP2 generated, delete tiny gray images less than 10kB
     * @param outputFile the output JP2
     */
    public void deleteTinyGrayVoidImages(String outputFile) throws Exception {
        File output = new File(outputFile);
        if (output.length() < 10000 && colorFieldsService.identifyType(outputFile).contains("Gray")) {
            Files.deleteIfExists(Path.of(outputFile));
        }
    }

    public String linkToOriginal(String fileName, String sourceFormat, List<String> intermediateFiles) throws Exception {
        String extension = FilenameUtils.getExtension(fileName);
        // Skip creating link if the extension matches the source format or is already in the source formats list
        if (extension.equals(sourceFormat) || SOURCE_FORMATS.containsKey(extension)) {
            return fileName;
        }
        Path target = Paths.get(fileName).toAbsolutePath();
        Path link = Files.createTempFile(tmpDir, FilenameUtils.getName(fileName), "." + sourceFormat);
        Files.delete(link);
        Files.createSymbolicLink(link, target);
        var linkPath = link.toAbsolutePath().toString();
        // register symlink for cleanup
        intermediateFiles.add(linkPath);

        return linkPath;
    }

    public void setColorFieldsService(ColorFieldsService colorFieldsService) {
        this.colorFieldsService = colorFieldsService;
    }

    public void setImagePreproccessingService(ImagePreproccessingService imagePreproccessingService) {
        this.imagePreproccessingService = imagePreproccessingService;
    }
}
