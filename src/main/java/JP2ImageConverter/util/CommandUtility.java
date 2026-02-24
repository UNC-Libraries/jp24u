package JP2ImageConverter.util;

import JP2ImageConverter.errors.CommandException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
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
        log.debug("Executing command: {}", String.join(" ", command));
        CommandLine cmdLine = CommandLine.parse(command.getFirst());
        cmdLine.addArguments(command.subList(1, command.size()).toArray(new String[0]));

        DefaultExecutor executor = DefaultExecutor.builder().get();
        var watchdog = ExecuteWatchdog.builder()
                .setTimeout(Duration.of(MAX_TIMEOUT_SECONDS, ChronoUnit.SECONDS))
                .get();
        executor.setWatchdog(watchdog);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(outputStream));

        try {
            executor.execute(cmdLine);
            return outputStream.toString();
        } catch (IOException e) {
            throw new CommandException("Command failed to execute", command, outputStream.toString(), e);
        }
    }

    /**
     * Run a given command and write the output to a file
     * @param command
     */
    public static void executeCommandWriteToFile(List<String> command, String temporaryFile) {
        log.debug("Executing command: {}", String.join(" ", command));
        CommandLine cmdLine = CommandLine.parse(command.getFirst());
        cmdLine.addArguments(command.subList(1, command.size()).toArray(new String[0]));

        DefaultExecutor executor = DefaultExecutor.builder().get();
        var watchdog = ExecuteWatchdog.builder()
                .setTimeout(Duration.of(MAX_TIMEOUT_SECONDS, ChronoUnit.SECONDS))
                .get();
        executor.setWatchdog(watchdog);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(byteArrayOutputStream));

        try {
            executor.execute(cmdLine);
            OutputStream outputStream = new FileOutputStream(temporaryFile);
            byteArrayOutputStream.writeTo(outputStream);
        } catch (IOException e) {
            throw new CommandException("Command failed to execute", command, byteArrayOutputStream.toString(), e);
        }
    }
}
