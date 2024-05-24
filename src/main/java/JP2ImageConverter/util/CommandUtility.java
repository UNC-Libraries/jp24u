package JP2ImageConverter.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public static void executeCommandWriteToFile(List<String> command, String temporaryFile) throws Exception {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            builder.redirectOutput(new File(temporaryFile));
            Process process = builder.start();

            if (process.waitFor() != 0) {
                throw new Exception("Command exited with status code " + process.waitFor());
            }
        } catch (Exception e) {
            throw new Exception("Command failed: " + command, e);
        }
    }
}
