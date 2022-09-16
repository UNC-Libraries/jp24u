import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author krwong
 */
public class ColorScanner {
    /**
     * Print file size of a given file (for now)
     */
    public static void main(String[] args) {
        if (args.length == 1 && args[0] != null) {
            String fileName = args[0];
            Path filePath = Paths.get(fileName);
            if (Files.exists(filePath)) {
                try {
                    long fileSize = Files.size(filePath);
                    System.out.println("File size: " + fileSize);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        } else {
            System.out.println("Error: File does not exist.");
        }
    }
}
