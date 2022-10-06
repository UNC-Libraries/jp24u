import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifInteropDirectory;
import com.drew.metadata.icc.IccDirectory;
import com.drew.metadata.photoshop.PsdHeaderDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        String colorMode = "\t";
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

        //Photoshop Header Tag(s): ColorMode
        if (metadata.containsDirectoryOfType(PsdHeaderDirectory.class)) {
            PsdHeaderDirectory psdHeaderDirectory = metadata.getFirstDirectoryOfType(PsdHeaderDirectory);
            if (psdHeaderDirectory.containsTag(PsdHeaderDirectory.TAG_COLOR_MODE)) {
                colorMode = psdHeaderDirectory.getDescription(PsdHeaderDirectory.TAG_COLOR_MODE);
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

        System.out.println("ICCProfileName:" + iccProfileName + "ColorSpace:" + colorSpace + "ColorMode:" + colorMode +
                "InteropIndex:" + interopIndex + "PhotometricInterpretation:" + photometricInterpretation);
    }

    /**
     * Print file size of a given file (for now)
     */
    public static void main(String[] args) throws Exception, IOException {
        if (args.length == 1 && !args[0].trim().isEmpty()) {
            String fileName = args[0];
            Path filePath = Paths.get(fileName);
            if (Files.exists(filePath)) {
                long fileSize = Files.size(filePath);
                System.out.println("File size: " + fileSize);
                colorFields(fileName);
            } else {
                System.out.println("Error: File does not exist.");
            }
        } else {
            System.out.println("Error: Please input one argument.");
        }
    }
}
