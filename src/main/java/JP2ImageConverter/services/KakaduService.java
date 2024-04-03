package JP2ImageConverter.services;

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
 * Supported image formats: TIFF, JPEG, PNG, GIF, PICT, BMP, PSD, JP2, NEF, CRW, CR2, DNG, RAF
 * @author krwong
 */
public class KakaduService {
    private static final Logger log = getLogger(KakaduService.class);

    public Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));

    private ColorFieldsService colorFieldsService;
    private ImagePreproccessingService imagePreproccessingService;

    /**
     * Get color space from EXIF fields
     * @param preprocessedImage a preprocessed image
     * @param originalImage the original input image
     * @param sourceFormat file extension/mimetype
     * @return colorSpace
     */
    public String getColorSpace(String preprocessedImage, String originalImage, String sourceFormat) throws Exception {
        String colorSpace;
        var preprocessedImageMetadata = extractMetadata(preprocessedImage, "");
        var originalImageMetadata = extractMetadata(originalImage, sourceFormat);
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

        return colorSpace;
    }

    /**
     * Extract metadata from files of formats supported by metadata-extractor.
     * Return an empty collection for unsupported types (JP2, PICT, PPM)
     * @param fileName an image file
     * @param sourceFormat file extension/mimetype override
     * @return imageMetadata
     */
    private Map<String, String> extractMetadata(String fileName, String sourceFormat) throws Exception {
        String extension = sourceFormat.isEmpty() ? FilenameUtils.getExtension(fileName).toLowerCase() : sourceFormat;
        if (extension.equals("jp2") || extension.equals("pct") || extension.equals("ppm")) {
            return Collections.emptyMap();
        }
        return colorFieldsService.colorFields(fileName);
    }

    /**
     * Run kdu_compress and convert image to JP2
     * @param fileName an image file
     * @param outputPath destination for converted files
     * @param sourceFormat file extension/mimetype override
     */
    public void kduCompress(String fileName, Path outputPath, String sourceFormat) throws Exception {
        // list of intermediate files to delete after JP2 is created
        List<String> intermediateFiles = new ArrayList<>();

        try {
            // override source file type detection with user-inputted image file type
            // accepted file types are listed in sourceFormats below
            Map<String, String> sourceFormats = new HashMap<>();
            sourceFormats.put("tiff", "tiff");
            sourceFormats.put("tif", "tiff");
            sourceFormats.put("image/tiff", "tiff");
            sourceFormats.put("jpeg", "jpeg");
            sourceFormats.put("jpg", "jpeg");
            sourceFormats.put("image/jpeg", "jpeg");
            sourceFormats.put("png", "png");
            sourceFormats.put("image/png", "png");
            sourceFormats.put("gif", "gif");
            sourceFormats.put("image/gif", "gif");
            sourceFormats.put("pict", "pct");
            sourceFormats.put("pct", "pct");
            sourceFormats.put("pic", "pct");
            sourceFormats.put("bmp", "bmp");
            sourceFormats.put("image/bmp", "bmp");
            sourceFormats.put("psd", "psd");
            sourceFormats.put("image/psd", "psd");
            sourceFormats.put("image/vnd.adobe.photoshop", "psd");
            sourceFormats.put("jp2", "jp2");
            sourceFormats.put("image/jp2", "jp2");
            sourceFormats.put("nef", "nef");
            sourceFormats.put("image/x-nikon-nef", "nef");
            sourceFormats.put("crw", "crw");
            sourceFormats.put("image/x-canon-crw", "crw");
            sourceFormats.put("cr2", "cr2");
            sourceFormats.put("image/x-canon-cr2", "cr2");
            sourceFormats.put("dng", "dng");
            sourceFormats.put("image/x-adobe-dng", "dng");
            sourceFormats.put("raf", "raf");
            sourceFormats.put("image/x-fujifilm-raf", "raf");

            if (!sourceFormat.isEmpty() && sourceFormats.containsKey(sourceFormat)) {
                sourceFormat = sourceFormats.get(sourceFormat);
                fileName = linkToOriginal(fileName, sourceFormat);
            } else if (!sourceFormat.isEmpty() && !sourceFormats.containsKey(sourceFormat)) {
                throw new Exception(sourceFormat + " file type is not supported.");
            }

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

            // get color space from colorFields
            String colorSpace = getColorSpace(inputFile, fileName, sourceFormat);

            // for unusual color spaces (CMYK): convert to temporary TIFF before kduCompress
            inputFile = imagePreproccessingService.convertColorSpaces(colorSpace, inputFile);
            intermediateFiles.add(inputFile);

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

            CommandUtility.executeCommand(command);
            deleteTinyGrayVoidImages(outputFile);
        } finally {
            // delete intermediate files and symlinks after JP2 generated
            for (String intermediateFile : intermediateFiles) {
                Files.deleteIfExists(Path.of(intermediateFile));
            }
        }
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

    public String linkToOriginal(String fileName, String sourceFormat) throws Exception {
        Path target = Paths.get(fileName).toAbsolutePath();
        Path link = Files.createTempFile(tmpDir, FilenameUtils.getName(fileName), "." + sourceFormat);
        Files.delete(link);
        Files.createSymbolicLink(link, target);

        return link.toAbsolutePath().toString();
    }

    public void setColorFieldsService(ColorFieldsService colorFieldsService) {
        this.colorFieldsService = colorFieldsService;
    }

    public void setImagePreproccessingService(ImagePreproccessingService imagePreproccessingService) {
        this.imagePreproccessingService = imagePreproccessingService;
    }
}
