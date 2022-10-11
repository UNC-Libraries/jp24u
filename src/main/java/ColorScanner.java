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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * @author krwong
 */
public class ColorScanner {
    /**
     * Print EXIF and ICC Profile fields
     */
    public static void colorFields(String fileName) throws Exception {
        String iccProfileName = "\t";
        String colorSpace = "\t";
        String interopIndex = "\t";
        String photometricInterpretation = "\t";

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
        fields.add("ICCProfileName:" + iccProfileName);
        fields.add("ColorSpace:" + colorSpace);
        fields.add("InteropIndex:" + interopIndex);
        fields.add("PhotometricInterpretation:" + photometricInterpretation);
        String allFields = String.join("\t", fields);
        System.out.print(allFields + "\t");
    }

    /**
     * Run identify command and print output
     */
    public static void identify(String fileName) throws IOException {
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
        System.out.print("\"");
        while ((line = br.readLine()) != null) {
            System.out.print(line);
        }
        System.out.println("\"");
    }

    /**
     * Print file size of a given file (for now)
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 1 && !args[0].trim().isEmpty()) {
            String fileName = args[0];
            Path filePath = Paths.get(fileName);
            if (Files.exists(filePath)) {
                long fileSize = Files.size(filePath);
                System.out.println("File size: " + fileSize);
                colorFields(fileName);
                identify(fileName);
            } else {
                System.out.println("Error: File does not exist.");
            }
        } else {
            System.out.println("Error: Please input one argument.");
        }
    }
}
