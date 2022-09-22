import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.Tag;
import com.thebuzzmedia.exiftool.core.StandardTag;

import java.awt.color.ICC_Profile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

/**
 * @author krwong
 */
public class ColorScanner {
    /**
     * Print EXIF and ICC Profile fields
     */
    public static void colorFields(String fileName) throws Exception {
        File imageFile = new File(fileName);
        Map<Tag, String> valueMap;
        try (ExifTool exifTool = new ExifToolBuilder().build()) {
            valueMap = exifTool.getImageMeta(imageFile, Arrays.asList(
                    StandardTag.COLOR_SPACE,
                    ));
        }
        System.out.println(valueMap.entrySet());
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
