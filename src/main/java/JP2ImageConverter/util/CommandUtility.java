package JP2ImageConverter.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Utility for executing commands
 * @author krwong
 */
public class CommandUtility {

    private CommandUtility() {
    }

    /**
     * Run a given command
     * @param command the command to be executed
     * @return command output
     */
    public static String executeCommand(List<String> command) throws Exception {
        String output = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            InputStream is = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = br.readLine()) != null) {
                output = line;
            }
            if (process.waitFor() != 0) {
                throw new Exception("Command exited with status code " + process.waitFor() + ": " + output);
            }
        } catch (Exception e) {
            throw new Exception("Command failed: " + command, e);
        }

        return output;
    }

    public static String executeCommandWriteToFile(List<String> command, String temporaryFile) throws Exception {
        String output = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            //builder.redirectInput(new File(temporaryFile));
            Process process = builder.start();
            InputStream is = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = br.readLine()) != null) {
                output = line;
            }
            if (process.waitFor() != 0) {
                throw new Exception("Command exited with status code " + process.waitFor() + ": " + output);
            }

            File targetFile = new File(temporaryFile);
            FileUtils.copyInputStreamToFile(is, targetFile);

        } catch (Exception e) {
            throw new Exception("Command failed: " + command, e);
        }

        return output;
    }
}
