import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifInteropDirectory;
import com.drew.metadata.icc.IccDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author krwong
 */
public class ColorScanner {
    /**
     * Return list of EXIF and ICC Profile fields
     * @return
     */
    public static List<String> colorFields(String fileName) throws Exception {
        String iccProfileName = null;
        String colorSpace = null;
        String interopIndex = null;
        String photometricInterpretation = null;

        File imageFile = new File(fileName);
        Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

        //ICC Profile Tag(s): ICCProfileName, ColorSpace
        if (metadata.containsDirectoryOfType(IccDirectory.class)) {
            IccDirectory iccDirectory = metadata.getFirstDirectoryOfType(IccDirectory.class);
            if (iccDirectory.containsTag(IccDirectory.TAG_TAG_desc)) {
                iccProfileName = iccDirectory.getDescription(IccDirectory.TAG_TAG_desc);
            }
            if (iccDirectory.containsTag(IccDirectory.TAG_COLOR_SPACE)) {
                colorSpace = iccDirectory.getDescription(IccDirectory.TAG_COLOR_SPACE);
            }
        }

        //EXIF InteropIFD Tag(s): InteropIndex
        if (metadata.containsDirectoryOfType(ExifInteropDirectory.class)) {
            ExifInteropDirectory exifInteropDirectory = metadata.getFirstDirectoryOfType(ExifInteropDirectory.class);
            if (exifInteropDirectory.containsTag(ExifInteropDirectory.TAG_INTEROP_INDEX)) {
                interopIndex = exifInteropDirectory.getDescription(ExifInteropDirectory.TAG_INTEROP_INDEX);
            }
        }

        //EXIF IFD0 Tag(s): PhotometricInterpretation
        if (metadata.containsDirectoryOfType(ExifIFD0Directory.class)) {
            ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (exifIFD0Directory.containsTag(ExifInteropDirectory.TAG_PHOTOMETRIC_INTERPRETATION)) {
                photometricInterpretation =
                        exifIFD0Directory.getDescription(ExifIFD0Directory.TAG_PHOTOMETRIC_INTERPRETATION);
            }
        }

        List<String> fields = new LinkedList<>();
        fields.add(fileName);
        fields.add("ICCProfileName:" + iccProfileName);
        fields.add("ColorSpace:" + colorSpace);
        fields.add("InteropIndex:" + interopIndex);
        fields.add("PhotometricInterpretation:" + photometricInterpretation);
        return fields;
    }

    /**
     * Run identify command and return attributes
     */
    public static String identify(String fileName) throws IOException {
        String identify = "identify" ;
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
     */
    public static void allFields(String fileName) throws Exception {
        List fields = colorFields(fileName);
        String attributes = identify(fileName);
        fields.add(attributes);
        String allFields = String.join("\t", fields);
        System.out.println(allFields);
    }

    /**
     * Read file in list
     */
    public static List<String> readFileInList(String fileName) throws IOException {
        List<String> listOfImageFiles = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
        return listOfImageFiles;
    }

    /**
     * Print image-related fields and attributes of a given file (workaround until picocli)
     */
    public static int executeCommand(String[] args) throws Exception {
        List<String> listOfFiles = new ArrayList<>();

        if (args.length == 1 && !args[0].trim().isEmpty()) {
            String fileName = args[0];
            listOfFiles.add(fileName);
        } else if (args.length == 2 && args[0].startsWith("-list")) {
            String fileName = args[1];
            if (Files.exists(Paths.get(fileName))) {
                listOfFiles = readFileInList(fileName);
            } else {
                System.out.println("Error: " + fileName + " does not exist.");
                return 1;
            }
        } else {
            System.out.println("Error: Please input an argument.");
            return 1;
        }

        if (listOfFiles.size() == 1 && !Files.exists(Paths.get(listOfFiles.get(0)))) {
            System.out.println("Error: " + listOfFiles.get(0) + " does not exist.");
            return 1;
        }

        Iterator<String> itr = listOfFiles.iterator();
        while (itr.hasNext()) {
            String imageFileName = itr.next();
            if (Files.exists(Paths.get(imageFileName))) {
                allFields(imageFileName);
            } else {
                System.out.println("Error: " + imageFileName + " does not exist.");
            }
        }
        return 0;
    }

    /**
     * Print image-related fields and attributes of a given file
     */
    public static void main(String[] args) throws Exception {
        System.exit(executeCommand(args));
    }
}
