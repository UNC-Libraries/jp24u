package colorscanner.services;

import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for converting a CMYK source image to a temporary image file before kakadu jp2 compression
 * @author krwong
 */
public class TemporaryImageService {
    private static final Logger log = getLogger(TemporaryImageService.class);

    //TODO: we will need to test different CMYK conversion options
    //It seems like only using Color Space creates a more color accurate temporary image.
    //Using Color Space and ICC Profile or just the ICC Profile create a temporary image with slightly different colors.
    public String convertImage(String fileName) throws Exception {
        String convert = "convert";
        //String profile = "-profile";
        //String profileOptions = "src/main/resources/AdobeRGB1998.icc";
        String colorSpace = "-colorspace";
        String colorSpaceOptions = "srgb";
        String temporaryFile = fileName + ".jpg";

        List<String> command = Arrays.asList(convert, fileName, colorSpace, colorSpaceOptions,
                temporaryFile);

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String cmdOutput = new String(process.getInputStream().readAllBytes());
            log.info(cmdOutput);
        } catch (Exception e) {
            throw new Exception(fileName + " failed to generate jpg file.", e);
        }

        return temporaryFile;
    }
}
