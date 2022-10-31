package colorscanner.services;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifInteropDirectory;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for retrieving image color fields and attributes
 * @author krwong
 */
public class ColorFieldsService {
    private static final Logger log = getLogger(ColorFieldsService.class);

    /**
     * Return list of EXIF and ICC Profile fields
     * @param fileName
     * @return List
     */
    public List<String> colorFields(String fileName) throws Exception {
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
     * @param fileName
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
     * @param fileName
     */
    public void listFields(String fileName) throws Exception {
        List fields = colorFields(fileName);
        String attributes = identify(fileName);
        fields.add(attributes);
        String allFields = String.join("\t", fields);
        System.out.println(allFields);
    }

    /**
     * Iterate through list of image files and return all color fields
     * @param fileName
     */
    public void fileListAllFields(String fileName) throws Exception {
        List<String> listOfFiles = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);

        Iterator<String> itr = listOfFiles.iterator();
        while (itr.hasNext()) {
            String imageFileName = itr.next();
            if (Files.exists(Paths.get(imageFileName))) {
                listFields(imageFileName);
            } else {
                throw new Exception(imageFileName + " does not exist. Not processing file list further.");
            }
        }
    }
}
