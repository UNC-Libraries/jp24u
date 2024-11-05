package JP2ImageConverter.util;

import JP2ImageConverter.errors.CommandException;
import JP2ImageConverter.errors.CommandTimeoutException;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Utility for executing commands
 * @author krwong
 */
public class CommandUtility {
    private static final Logger log = getLogger(CommandUtility.class);
    private static final int MAX_TIMEOUT_SECONDS = System.getProperty("jp24u.subcommand.timeout") != null ?
            Integer.parseInt(System.getProperty("jp24u.subcommand.timeout")) : 60 * 5;

    private CommandUtility() {
    }

    /**
     * Run a given command
     * @param command the command to be executed
     * @return command output
     */
    public static String executeCommand(List<String> command) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            // Use a separate thread to read the output concurrently, to avoid deadlock in case command times out
            var outputReaderTask = getOutputReaderTask(process.getInputStream(), output);

            waitForProcess(process, command, outputReaderTask, output);
            // If any errors occurred while reading the output, they will be thrown here
            outputReaderTask.join();
            if (process.exitValue() != 0) {
                throw new CommandException("Command exited with errors", command, output.toString(), process.exitValue());
            }
        } catch (InterruptedException | IOException e) {
            throw new CommandException("Command failed to execute", command, output.toString(), e);
        }

        return output.toString();
    }

    /**
     * Run a given command and write the output to a file
     * @param command
     * @param temporaryFile
     */
    public static void executeCommandWriteToFile(List<String> command, String temporaryFile) {
        StringBuilder errorOutput = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectOutput(new File(temporaryFile));
            Process process = builder.start();

            // Use a separate thread to read the errors concurrently, to keep it separate from the main file output
            var outputReaderTask = getOutputReaderTask(process.getErrorStream(), errorOutput);

            waitForProcess(process, command, outputReaderTask, errorOutput);
            if (process.exitValue() != 0) {
                throw new CommandException("Command failed", command, errorOutput.toString(), process.exitValue());
            }
        } catch (InterruptedException | IOException e) {
            throw new CommandException("Command failed to execute", command, errorOutput.toString(), e);
        }
    }

    private static CompletableFuture<Void> getOutputReaderTask(InputStream inputStream, StringBuilder output) {
        return CompletableFuture.runAsync(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            } catch (IOException e) {
                throw new CommandException("Error reading command output", null, output.toString(), e);
            }
        });
    }

    private static void waitForProcess(Process process, List<String> command, CompletableFuture<Void> outputFuture,
                                       StringBuilder output)
            throws InterruptedException {
        log.debug("Waiting for process for {} seconds: {}", MAX_TIMEOUT_SECONDS, command);
        if (!process.waitFor(MAX_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            log.warn("Command timed out, attempting to end process: {}", command);
            process.destroy();
            if (outputFuture != null) {
                outputFuture.join();
            }
            throw new CommandTimeoutException("Command timed out after " + MAX_TIMEOUT_SECONDS + " seconds",
                    command, output.toString());
        }
    }
}
