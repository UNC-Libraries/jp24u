package JP2ImageConverter.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import JP2ImageConverter.errors.CommandException;
import JP2ImageConverter.util.CommandUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author bbpennel
 */
public class CommandUtilityTest {

    @TempDir
    Path tempDir;

    @Test
    public void testExecuteCommandSuccess() throws IOException {
        // Create platform-specific command to echo text
        List<String> command = getPlatformEchoCommand("test output");

        // Execute the command
        String result = CommandUtility.executeCommand(command);

        // Verify results
        assertTrue(result.contains("test output"));
    }

    @Test
    public void testExecuteCommandWithStdErr() throws IOException {
        // Create script that outputs to stderr
        Path scriptPath = createExecutableScript(
                "#!/bin/sh\n" +
                        "echo 'standard output'\n" +
                        "echo 'error output' >&2\n");

        // Execute the command
        String result = CommandUtility.executeCommand(List.of(scriptPath.toString()));

        // Verify results
        assertTrue(result.contains("standard output\n"));
        assertTrue(result.contains("error output\n"));
    }

    @Test
    public void testExecuteCommandNonZeroExit() throws IOException {
        // Create script that exits with non-zero
        Path scriptPath = createExecutableScript(
                "#!/bin/sh\n" +
                        "echo 'some output'\n" +
                        "exit 1\n");

        // Execute the command and verify it throws CommandException
        CommandException exception = assertThrows(CommandException.class, () ->
                CommandUtility.executeCommand(List.of(scriptPath.toString())));

        assertEquals(1, exception.getExitCode());
        assertTrue(exception.getMessage().contains("Command failed to execute"));
        assertTrue(exception.getOutput().contains("some output"));
    }

    @Test
    public void testExecuteInvalidCommand() {
        // Try to execute non-existent command
        CommandException exception = assertThrows(CommandException.class, () ->
                CommandUtility.executeCommand(List.of("thisCommandDoesNotExist_" + System.currentTimeMillis())));

        assertTrue(exception.getMessage().contains("Command failed to execute"));
    }

    @Test
    public void testExecuteCommandWithSpacesAndDashesInPath() throws IOException {
        // Create a temp file with spaces and dashes in the name
        Path filePath = tempDir.resolve("West End Poets News letter no97 2026 Mar-Apr-May.pdf");
        Files.createFile(filePath);

        // Script outputs the number of arguments received, followed by each argument on its own line.
        // If the path is incorrectly split on spaces, $# will be > 2 and the full path won't appear
        // as a single argument.
        Path scriptPath = createExecutableScript(
                "#!/bin/sh\n" +
                        "echo \"arg_count=$#\"\n" +
                        "for arg in \"$@\"; do echo \"arg=$arg\"; done\n");

        String result = CommandUtility.executeCommand(
                List.of(scriptPath.toString(), "-i", filePath.toString()));

        // Exactly 2 arguments should be received: "-i" and the full path as one token
        assertTrue(result.contains("arg_count=2"),
                "Path with spaces should be passed as a single argument, but output was: " + result);
        assertTrue(result.contains("arg=" + filePath),
                "Output should contain the full file path as a single argument, but output was: " + result);
    }

    /**
     * Creates platform-specific command to echo text
     */
    private List<String> getPlatformEchoCommand(String text) {
        return Arrays.asList("echo", text);
    }

    /**
     * Creates an executable script file with the given content
     */
    private Path createExecutableScript(String scriptContent) throws IOException {
        Path scriptPath = tempDir.resolve("test_script.sh");
        Files.writeString(scriptPath, scriptContent);
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(scriptPath, perms);

        return scriptPath;
    }
}
