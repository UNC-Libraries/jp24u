package colorscanner.services;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for kakadu kduCompress
 * Supported image formats: TIFF, JPEG, PNG, GIF, PICT, BMP
 * @author krwong
 */
public class KakaduService {
    private static final Logger log = getLogger(KakaduService.class);

    private ColorFieldsService colorFieldsService;
    private TemporaryImageService temporaryImageService;

    /**
     * Get ColorSpace from exif fields
     * @param fileName an image file
     * @return colorSpace
     */
    public String getColorSpace(String fileName) throws Exception {
        // we will check 2 exif fields (ColorSpace and PhotometricInterpretation) for color space information
        String colorSpace = "null";
        Map<String,String> imageMetadata = colorFieldsService.colorFields(fileName);

        if (imageMetadata.get(ColorFieldsService.COLOR_SPACE) != null) {
            colorSpace = imageMetadata.get(ColorFieldsService.COLOR_SPACE);
        } else if (imageMetadata.get(ColorFieldsService.PHOTOMETRIC_INTERPRETATION) != null) {
            colorSpace = imageMetadata.get(ColorFieldsService.PHOTOMETRIC_INTERPRETATION);
        } else {
            log.info(fileName + ": colorSpace information not found.");
        }

        if (colorSpace.toLowerCase().matches("blackiszero") ||
                colorSpace.toLowerCase().matches("whiteiszero")) {
            colorSpace = "Gray";
        }

        return colorSpace;
    }

    /**
     * Run kdu_compress and convert image to jp2
     * @param fileName an image file
     */
    public void kduCompress(String fileName, String outputPath) throws Exception {
        String kduCompress = "kdu_compress";
        String input = "-i";
        String inputFile;
        String output = "-o";
        String outputFile = outputPath + "/" + FilenameUtils.getBaseName(fileName) + ".jp2";
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

        // for non-TIFF image formats: convert to temp tiff before kdu_compress
        // currently supported image formats: JPEG, PNG, GIF, PICT, BMP
        String fileNameExtension = FilenameUtils.getExtension(fileName).toLowerCase();
        Set<String> imageFormats = new HashSet<>(Arrays.asList("jpeg", "jpg", "png", "gif", "pct", "bmp"));
        if (imageFormats.contains(fileNameExtension)) {
            inputFile = temporaryImageService.convertImageFormats(fileName);
        } else {
            inputFile = fileName;
        }

        // get color space from colorFields
        String colorSpace = getColorSpace(inputFile);
        //for CMYK images: convert to temporary tiff before kduCompress
        if (colorSpace.toLowerCase().contains("cmyk")) {
            inputFile = temporaryImageService.convertCmykColorSpace(fileName);
        }

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

    /**
     * Iterate through list of image files and run kdu_compress to convert all tifs to jp2s
     * @param fileName a list of image files
     */
    public void fileListKduCompress(String fileName, String outputPath) throws Exception {
        List<String> listOfFiles = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);

        Iterator<String> itr = listOfFiles.iterator();
        while (itr.hasNext()) {
            String imageFileName = itr.next();
            if (Files.exists(Paths.get(imageFileName))) {
                kduCompress(imageFileName, outputPath);
            } else {
                throw new Exception(imageFileName + " does not exist. Not processing file list further.");
            }
        }
    }

    public void setColorFieldsService(ColorFieldsService colorFieldsService) {
        this.colorFieldsService = colorFieldsService;
    }

    public void setTemporaryImageService(TemporaryImageService temporaryImageService) {
        this.temporaryImageService = temporaryImageService;
    }
}
