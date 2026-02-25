package JP2ImageConverter.util;

import JP2ImageConverter.errors.CommandException;
import JP2ImageConverter.errors.CommandTimeoutException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

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
        log.debug("Executing command with timeout {}s: {}", MAX_TIMEOUT_SECONDS, String.join(" ", command));
        CommandLine cmdLine = CommandLine.parse(command.getFirst());
        cmdLine.addArguments(command.subList(1, command.size()).toArray(new String[0]));

        DefaultExecutor executor = DefaultExecutor.builder().get();
        ExecuteWatchdog watchdog = null;
        if (MAX_TIMEOUT_SECONDS > 0) {
            watchdog = EscalatingExecuteWatchdog.create(Duration.ofSeconds(MAX_TIMEOUT_SECONDS));
            executor.setWatchdog(watchdog);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(outputStream, errorStream));

        try {
            executor.execute(cmdLine);
            return outputStream.toString();
        } catch (ExecuteException e) {
            String output = outputStream.toString();
            int exitValue = e.getExitValue();

            if (watchdog != null && watchdog.killedProcess()) {
                throw new CommandTimeoutException("Command timed out after " + MAX_TIMEOUT_SECONDS + " seconds",
                        command, output);
            }
            throw new CommandException("Command failed to execute", command, output, exitValue, e);
        } catch (IOException e) {
            String output = outputStream + "\n" + errorStream;
            throw new CommandException("Command failed to execute", command, output, e);
        }
    }

    /**
     * Run a given command and write the output to a file
     * @param command
     */
    public static void executeCommandWriteToFile(List<String> command, String temporaryFile) {
        log.debug("Executing command with timeout {}s: {}", MAX_TIMEOUT_SECONDS, String.join(" ", command));
        CommandLine cmdLine = CommandLine.parse(command.getFirst());
        cmdLine.addArguments(command.subList(1, command.size()).toArray(new String[0]));

        DefaultExecutor executor = DefaultExecutor.builder().get();
        ExecuteWatchdog watchdog = null;
        if (MAX_TIMEOUT_SECONDS > 0) {
            watchdog = EscalatingExecuteWatchdog.create(Duration.ofSeconds(MAX_TIMEOUT_SECONDS));
            executor.setWatchdog(watchdog);
        }

        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        try (FileOutputStream outputStream = new FileOutputStream(temporaryFile)) {
            executor.setStreamHandler(new PumpStreamHandler(outputStream, errorStream));
            executor.execute(cmdLine);
        } catch (ExecuteException e) {
            int exitValue = e.getExitValue();

            if (watchdog != null && watchdog.killedProcess()) {
                throw new CommandTimeoutException("Command timed out after " + MAX_TIMEOUT_SECONDS + " seconds",
                        command, temporaryFile + "\n" + errorStream);
            }
            throw new CommandException("Command failed to execute", command, temporaryFile + "\n" + errorStream,
                    exitValue, e);
        } catch (IOException e) {
            throw new CommandException("Command failed to execute", command, temporaryFile + "\n" + errorStream, e);
        }
    }
}
