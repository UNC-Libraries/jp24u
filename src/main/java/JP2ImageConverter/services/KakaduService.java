package JP2ImageConverter.services;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for Kakadu kduCompress
 * Supported image formats: TIFF, JPEG, PNG, GIF, PICT, BMP
 * @author krwong
 */
public class KakaduService {
    private static final Logger log = getLogger(KakaduService.class);

    private ColorFieldsService colorFieldsService;
    private ImagePreproccessingService imagePreproccessingService;

    /**
     * Get color space from EXIF fields
     * @param fileName an image file
     * @return colorSpace
     */
    public String getColorSpace(String fileName) throws Exception {
        // we will check 2 EXIF fields (ColorSpace and PhotometricInterpretation) for color space information
        String colorSpace = "null";
        Map<String,String> imageMetadata = colorFieldsService.colorFields(fileName);

        if (imageMetadata.get(ColorFieldsService.COLOR_SPACE) != null) {
            colorSpace = imageMetadata.get(ColorFieldsService.COLOR_SPACE);
        } else if (imageMetadata.get(ColorFieldsService.PHOTOMETRIC_INTERPRETATION) != null) {
            colorSpace = imageMetadata.get(ColorFieldsService.PHOTOMETRIC_INTERPRETATION);
        } else {
            log.info(fileName + ": color space information not found.");
        }

        if (colorSpace.toLowerCase().matches("blackiszero") ||
                colorSpace.toLowerCase().matches("whiteiszero")) {
            colorSpace = "Gray";
        }

        return colorSpace;
    }

    /**
     * Run kdu_compress and convert image to JP2
     * @param fileName an image file, outputPath destination for converted files,
     *                 sourceFormat file extension/mimetype override
     */
    public void kduCompress(String fileName, String outputPath, String sourceFormat) throws Exception {
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

        if (!sourceFormat.isEmpty() && sourceFormats.containsKey(sourceFormat)) {
            sourceFormat = sourceFormats.get(sourceFormat);
        } else if (!sourceFormat.isEmpty() && !sourceFormats.containsKey(sourceFormat)) {
            throw new Exception(sourceFormat + " file type is not supported.");
        }

        String kduCompress = "kdu_compress";
        String input = "-i";
        String inputFile = imagePreproccessingService.convertToTiff(fileName, sourceFormat);
        String output = "-o";
        String outputFile;

        String outputDirectory = outputPath.substring(0, outputPath.lastIndexOf("/"));
        if (!outputPath.isEmpty() && Files.exists(Paths.get(outputDirectory))) {
            //add _deriv to access JP2 output to avoid overwriting preservation-quality JP2
            if (FilenameUtils.getExtension(fileName).toLowerCase().matches("jp2")) {
                outputFile = outputPath + "_deriv.jp2";
            } else {
                outputFile = outputPath + ".jp2";
            }
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
        String colorSpace = getColorSpace(inputFile);
        // for unusual color spaces (CMYK): convert to temporary TIFF before kduCompress
        inputFile = imagePreproccessingService.convertColorSpaces(colorSpace, inputFile);

        List<String> command = new ArrayList<>(Arrays.asList(kduCompress, input, inputFile, output, outputFile,
                clevels, clayers, cprecincts, stiles, corder, orggenplt, orgtparts, cblk, cusesop, cuseeph,
                flushPeriod, flushPeriodOptions, rate, rateOptions, weights));

        // for GIF images: add no_palette to command
        if (fileName.toLowerCase().endsWith("gif")) {
            noPalette = "-no_palette";
            command.add(noPalette);
        }

        // for grayscale images: add jp2Space to command
        if (colorSpace.toLowerCase().contains("gray")) {
            jp2Space = "-jp2_space";
            jp2SpaceOptions = "sLUM";
            command.add(jp2Space);
            command.add(jp2SpaceOptions);
        }

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String cmdOutput = new String(process.getInputStream().readAllBytes());
            log.info(cmdOutput);
        } catch (Exception e) {
            throw new Exception(fileName + " failed to generate jp2 file.", e);
        }
    }

//    /**
//     * Iterate through list of image files and run kdu_compress to convert all images to JP2s
//     * @param fileName a list of image files, outputPath destination for converted files,
//     *                 sourceFormat file extension/mimetype override
//     */
//    public void fileListKduCompress(String fileName, String outputPath, String sourceFormat) throws Exception {
//        List<String> listOfFiles = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
//
//        Iterator<String> itr = listOfFiles.iterator();
//        while (itr.hasNext()) {
//            String imageFileName = itr.next();
//            if (Files.exists(Paths.get(imageFileName))) {
//                kduCompress(imageFileName, outputPath, sourceFormat);
//            } else {
//                throw new Exception(imageFileName + " does not exist. Not processing file list further.");
//            }
//        }
//    }

    public void setColorFieldsService(ColorFieldsService colorFieldsService) {
        this.colorFieldsService = colorFieldsService;
    }

    public void setImagePreproccessingService(ImagePreproccessingService imagePreproccessingService) {
        this.imagePreproccessingService = imagePreproccessingService;
    }
}
