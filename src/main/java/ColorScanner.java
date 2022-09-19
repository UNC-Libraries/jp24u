import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author krwong
 */
public class ColorScanner {
    /**
     * Print file size of a given file (for now)
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 1 && !args[0].trim().isEmpty()) {
            String fileName = args[0];
            Path filePath = Paths.get(fileName);
            if (Files.exists(filePath)) {
                long fileSize = Files.size(filePath);
                System.out.println("File size: " + fileSize);
            } else {
                System.out.println("Error: File does not exist.");
            }
        } else {
            System.out.println("Error: Please input one argument.");
        }
    }
}
