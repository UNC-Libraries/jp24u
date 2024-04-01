package JP2ImageConverter.util;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class CommandUtility {
    private static final Logger log = getLogger(CommandUtility.class);

    public static void generateImage(List<String> command) throws Exception {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String cmdOutput = new String(process.getInputStream().readAllBytes());
            log.debug(cmdOutput);
            if (process.waitFor() != 0) {
                throw new Exception("Command exited with status code " + process.waitFor());
            }
        } catch (Exception e) {
            throw new Exception("Command failed: " + command, e);
        }
    }

    public static String identifyFileType(List<String> command) throws Exception {
        String imageType = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            InputStream is = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = br.readLine()) != null) {
                imageType = line;
            }
            if (process.waitFor() != 0) {
                throw new Exception("Command exited with status code " + process.waitFor());
            }
        } catch (Exception e) {
            throw new Exception(command + " failed to identity file type");
        }

        return imageType;
    }
}
