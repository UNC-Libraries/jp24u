package colorscanner.services;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifInteropDirectory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.file.FileSystemDirectory;
import com.drew.metadata.icc.IccDirectory;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for retrieving image color fields and attributes
 * @author krwong
 */
public class ColorFieldsService {
    private static final Logger log = getLogger(ColorFieldsService.class);

    public static final String IMAGE_FILE_NAME = "ImageFileName";
    public static final String FILE_SIZE = "FileSize";
    public static final String FILE_MODIFIED_DATE = "FileModifiedDate";
    public static final String DATE_TIME_ORIGINAL = "DateTimeOriginal";
    public static final String DATE_TIME_DIGITIZED = "DateTimeDigitized";
    public static final String ICC_PROFILE_NAME = "ICCProfileName";
    public static final String COLOR_SPACE = "ColorSpace";
    public static final String INTEROP_INDEX = "InteropIndex";
    public static final String PHOTOMETRIC_INTERPRETATION = "PhotometricInterpretation";
    public static final String MAGICK_IDENTIFY = "MagickIdentify";

    /**
     * Return list of EXIF and ICC Profile fields
     * @param fileName an image file
     * @return map of color fields
     */
    public Map<String,String> colorFields(String fileName) throws Exception {
        String fileSize = null;
        String fileModifiedDate = null;
        String dateTimeOriginal = null;
        String dateTimeDigitized = null;
        String iccProfileName = null;
        String colorSpace = null;
        String interopIndex = null;
        String photometricInterpretation = null;

        File imageFile = new File(fileName);
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

            //ICC Profile Tag(s): ICCProfileName, ColorSpace
            if (metadata.containsDirectoryOfType(IccDirectory.class)) {
                IccDirectory iccDirectory = metadata.getFirstDirectoryOfType(IccDirectory.class);
                if (iccDirectory.containsTag(IccDirectory.TAG_TAG_desc)) {
                    iccProfileName = iccDirectory.getDescription(IccDirectory.TAG_TAG_desc).trim();
                }
                if (iccDirectory.containsTag(IccDirectory.TAG_COLOR_SPACE)) {
                    colorSpace = iccDirectory.getDescription(IccDirectory.TAG_COLOR_SPACE).trim();
                }
            }

            //File System Tag(s): FileSize
            if (metadata.containsDirectoryOfType(FileSystemDirectory.class)) {
                FileSystemDirectory fileSystemDirectory = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);
                if (fileSystemDirectory.containsTag(FileSystemDirectory.TAG_FILE_SIZE)) {
                    fileSize = fileSystemDirectory.getDescription(FileSystemDirectory.TAG_FILE_SIZE).trim();
                }
                if (fileSystemDirectory.containsTag(FileSystemDirectory.TAG_FILE_MODIFIED_DATE)) {
                    fileModifiedDate = fileSystemDirectory.getDescription(FileSystemDirectory.TAG_FILE_MODIFIED_DATE).trim();
                }
            }

            //EXIF SubIFD Tag(s): DateTimeOriginal, DateTimeDigitized
            if (metadata.containsDirectoryOfType(ExifSubIFDDirectory.class)) {
                ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                if (exifSubIFDDirectory.containsTag(ExifInteropDirectory.TAG_DATETIME_ORIGINAL)) {
                    dateTimeOriginal = exifSubIFDDirectory.getDescription(ExifIFD0Directory.TAG_DATETIME_ORIGINAL).trim();
                }
                if (exifSubIFDDirectory.containsTag(ExifInteropDirectory.TAG_DATETIME_DIGITIZED)) {
                    dateTimeDigitized = exifSubIFDDirectory.getDescription(ExifIFD0Directory.TAG_DATETIME_DIGITIZED).trim();
                }
            }

            //EXIF InteropIFD Tag(s): InteropIndex
            if (metadata.containsDirectoryOfType(ExifInteropDirectory.class)) {
                ExifInteropDirectory exifInteropDirectory = metadata.getFirstDirectoryOfType(ExifInteropDirectory.class);
                if (exifInteropDirectory.containsTag(ExifInteropDirectory.TAG_INTEROP_INDEX)) {
                    interopIndex = exifInteropDirectory.getDescription(ExifInteropDirectory.TAG_INTEROP_INDEX).trim();
                }
            }

            //EXIF IFD0 Tag(s): PhotometricInterpretation
            if (metadata.containsDirectoryOfType(ExifIFD0Directory.class)) {
                ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                if (exifIFD0Directory.containsTag(ExifInteropDirectory.TAG_PHOTOMETRIC_INTERPRETATION)) {
                    photometricInterpretation =
                            exifIFD0Directory.getDescription(ExifIFD0Directory.TAG_PHOTOMETRIC_INTERPRETATION).trim();
                }
            }
        } catch (ImageProcessingException | IOException e) {
            log.error("Error reading image metadata for file {}", fileName, e);
            System.out.println("Error reading image metadata for file " + fileName);
        }

        //image metadata: ImageFileName, FileSize, FileModifiedDate, DateTimeOriginal, DateTimeDigitized,
        //ICCProfileName, ColorSpace, InteropIndex, PhotometricInterpretation
        Map<String, String> imageMetadata = new LinkedHashMap<>();
        imageMetadata.put(IMAGE_FILE_NAME, fileName);
        imageMetadata.put(FILE_SIZE, fileSize);
        imageMetadata.put(FILE_MODIFIED_DATE, fileModifiedDate);
        imageMetadata.put(DATE_TIME_ORIGINAL, dateTimeOriginal);
        imageMetadata.put(DATE_TIME_DIGITIZED, dateTimeDigitized);
        imageMetadata.put(ICC_PROFILE_NAME, iccProfileName);
        imageMetadata.put(COLOR_SPACE, colorSpace);
        imageMetadata.put(INTEROP_INDEX, interopIndex);
        imageMetadata.put(PHOTOMETRIC_INTERPRETATION, photometricInterpretation);

        return imageMetadata;
    }

    /**
     * Run identify command and return attributes
     * @param fileName an image file
     * @return list of color attributes
     */
    public String identify(String fileName) throws IOException {
        String identify = "identify";
        String quiet = "-quiet";
        String format = "-format";
        String options = "Dimensions: %wx%h;Channels: %[channels];Bit-depth: %[bit-depth];" +
                "Alpha channel: %A;Color Space: %[colorspace];Profiles: %[profiles];" +
                "ICC Profile: %[profile:icc];ICM Profile: %[profile:icm];";
        String[] command = {identify, quiet, format, options, fileName};

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        InputStream is = process.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        String attributes = "\"";
        while ((line = br.readLine()) != null) {
            attributes = attributes + line;
        }
        return attributes + "\"";
    }

    /**
     * Combine then print fields and attributes
     * @param fileName an image file
     * @return map of all color fields and attributes
     */
    public void listFields(String fileName) throws Exception {
        Map<String, String> imageMetadata = colorFields(fileName);
        String attributes = identify(fileName);
        imageMetadata.put(MAGICK_IDENTIFY, attributes);

        for (Map.Entry<String, String> entry : imageMetadata.entrySet()) {
            System.out.print(entry.getKey() + ":" + entry.getValue() + "\t");
        }
        System.out.println();
    }

    /**
     * Iterate through list of image files and return all color fields
     * @param fileName a list of image files
     */
    public void fileListAllFields(String fileName) throws Exception {
        Instant start = Instant.now();
        List<String> listOfFiles = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);

        int filesProcessed = 0;
        Iterator<String> itr = listOfFiles.iterator();
        while (itr.hasNext()) {
            String imageFileName = itr.next();
            if (Files.exists(Paths.get(imageFileName))) {
                listFields(imageFileName);
                filesProcessed++;
            } else {
                log.info(imageFileName + " does not exist.");
                System.out.println(imageFileName + " does not exist.");
            }
        }

        Instant end = Instant.now();
        Long overallRuntime = Duration.between(start, end).toMillis();
        Long runtimePerFile = overallRuntime / filesProcessed;
        System.out.println("Number of Files Processed: " + filesProcessed);
        System.out.println("Total Overall Runtime: " + overallRuntime + " milliseconds");
        System.out.println("Average Runtime per File: " + runtimePerFile + " milliseconds/file");

//        log.info("Number of Files Processed: " + filesProcessed);
//        log.info("Total Overall Runtime: " + overallRuntime + " seconds");
//        log.info("Average Runtime per File: " + runtimePerFile + " seconds/file");
    }
}
